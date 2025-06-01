package org.abr.memearenabot.service;

import org.abr.memearenabot.model.Meme;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.repository.MemeRepository;
import org.abr.memearenabot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing contests and awarding winners
 */
@Service
public class ContestService {
    private static final Logger logger = LoggerFactory.getLogger(ContestService.class);
    private static final int REQUIRED_PARTICIPANTS = 33;
    private static final String LOG_PREFIX = "Contest: ";

    private final MemeRepository memeRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    @Autowired
    public ContestService(MemeRepository memeRepository, UserRepository userRepository, MessageService messageService) {
        this.memeRepository = memeRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
        logger.info("{}Service initialized", LOG_PREFIX);
    }

    /**
     * Get current contest memes
     *
     * @return List of memes in current contest
     */
    public List<Meme> getCurrentContestMemes() {
        return memeRepository.findByInContestIsTrue();
    }

    /**
     * Get count of memes in current contest
     *
     * @return Number of participants in current contest
     */
    public int getCurrentContestParticipantsCount() {
        return memeRepository.countByInContestIsTrue();
    }

    /**
     * Submit meme to contest
     *
     * @param memeUrl URL of the meme to submit
     * @param userId  ID of the user submitting the meme
     * @return true if submission was successful, false otherwise
     */
    @Transactional
    public boolean submitMemeToContest(String memeUrl, String userId) {
        Optional<Meme> memeOpt = findMemeByUrl(memeUrl);

        if (memeOpt.isPresent()) {
            Meme meme = memeOpt.get();
            meme.setInContest(true);
            memeRepository.save(meme);
            logger.info("{}Meme {} submitted to contest by user {}", LOG_PREFIX, meme.getId(), userId);

            // Check if we reached the required number of participants
            if (getCurrentContestParticipantsCount() >= REQUIRED_PARTICIPANTS) {
                logger.info("{}Contest reached {} participants, ending automatically", LOG_PREFIX,
                        REQUIRED_PARTICIPANTS);
                return endContestAndAwardWinner();
            }

            return true;
        }

        logger.warn("{}Failed to submit meme to contest: meme with URL {} not found", LOG_PREFIX, memeUrl);
        return false;
    }

    /**
     * Find meme by URL
     *
     * @param memeUrl URL of the meme to find
     * @return Optional containing the meme if found
     */
    private Optional<Meme> findMemeByUrl(String memeUrl) {
        return memeRepository.findAll().stream().filter(meme -> meme.getImageUrl().equals(memeUrl)).findFirst();
    }

    /**
     * End current contest and award winner
     * Runs weekly on Sunday at midnight if contest hasn't ended automatically
     */
    @Scheduled(cron = "0 0 0 * * SUN")
    @Transactional
    public void scheduledContestEnd() {
        logger.info("{}Running scheduled contest end check", LOG_PREFIX);
        if (getCurrentContestParticipantsCount() > 0) {
            boolean result = endContestAndAwardWinner();
            logger.info("{}Scheduled contest end completed with result: {}", LOG_PREFIX, result);
        } else {
            logger.info("{}No active contest to end", LOG_PREFIX);
        }
    }

    /**
     * End contest and award winner
     *
     * @return true if contest was successfully ended and winner awarded, false otherwise
     */
    @Transactional
    public boolean endContestAndAwardWinner() {
        logger.info("{}Ending current contest and selecting winner", LOG_PREFIX);

        // Get contest memes sorted by likes
        List<Meme> contestMemes = memeRepository.findByInContestIsTrueOrderByLikesDesc();

        if (contestMemes.isEmpty()) {
            logger.info("{}No memes in contest, skipping winner selection", LOG_PREFIX);
            return false;
        }

        // Select winner (meme with most likes)
        Meme winnerMeme = contestMemes.get(0);

        // Award premium status to winner
        Optional<User> userOpt = userRepository.findByTelegramId(winnerMeme.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            awardPremiumStatus(user);

            logger.info("{}User {} awarded premium status for winning contest with meme {}", LOG_PREFIX,
                    user.getTelegramId(), winnerMeme.getId());
        } else {
            logger.warn("{}Winner user {} not found, premium status not awarded", LOG_PREFIX, winnerMeme.getUserId());
        }

        // Reset contest status for all memes
        resetContestStatus(contestMemes);

        logger.info("{}Contest ended, all memes reset to non-contest status", LOG_PREFIX);
        return true;
    }

    /**
     * Award premium status to user
     *
     * @param user User to award premium status to
     */
    private void awardPremiumStatus(User user) {
        user.setIsPremium(true);
        userRepository.save(user);
    }

    /**
     * Reset contest status for all memes
     *
     * @param contestMemes List of memes to reset
     */
    private void resetContestStatus(List<Meme> contestMemes) {
        for (Meme meme : contestMemes) {
            meme.setInContest(false);
            memeRepository.save(meme);
        }
    }

    /**
     * Admin command to manually end contest
     *
     * @return Message to display to admin
     */
    @Transactional
    public String adminEndContest() {
        List<Meme> contestMemes = memeRepository.findByInContestIsTrueOrderByLikesDesc();

        if (contestMemes.isEmpty()) {
            return messageService.getMessage("contest.no.memes");
        }

        Meme winnerMeme = contestMemes.get(0);
        Optional<User> userOpt = userRepository.findByTelegramId(winnerMeme.getUserId());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            awardPremiumStatus(user);
            resetContestStatus(contestMemes);

            logger.info("{}Contest manually ended by admin, user {} awarded premium status", LOG_PREFIX,
                    user.getTelegramId());

            String displayName = user.getUsername() != null ? "@" + user.getUsername() : user.getFirstName();
            return messageService.getMessage("contest.ended.winner", displayName);
        }

        logger.error("{}Error ending contest: winner user {} not found", LOG_PREFIX, winnerMeme.getUserId());
        return messageService.getMessage("contest.error");
    }

    /**
     * Get contest status message
     *
     * @return Message describing current contest status
     */
    public String getContestStatusMessage() {
        int participantsCount = getCurrentContestParticipantsCount();
        int remaining = REQUIRED_PARTICIPANTS - participantsCount;

        if (participantsCount == 0) {
            return messageService.getMessage("contest.status.empty");
        } else if (remaining > 0) {
            return messageService.getMessage("contest.status.progress", participantsCount, REQUIRED_PARTICIPANTS,
                    remaining);
        } else {
            return messageService.getMessage("contest.status.full");
        }
    }
} 