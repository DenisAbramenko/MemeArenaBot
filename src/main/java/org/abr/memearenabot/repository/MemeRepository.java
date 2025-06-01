package org.abr.memearenabot.repository;

import org.abr.memearenabot.model.Meme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Meme entities
 * Provides methods for accessing and manipulating meme data
 */
@Repository
public interface MemeRepository extends JpaRepository<Meme, Long> {

    /**
     * Find top 10 memes by likes (descending order)
     *
     * @return List of top 10 memes by likes
     */
    List<Meme> findTop10ByOrderByLikesDesc();

    /**
     * Find all memes currently in contest
     *
     * @return List of memes with inContest flag set to true
     */
    List<Meme> findByInContestIsTrue();

    /**
     * Find memes by user ID
     *
     * @param userId Telegram ID of the user
     * @return List of memes created by the specified user
     */
    List<Meme> findByUserId(String userId);

    /**
     * Find memes by type
     *
     * @param type Type of meme (AI_GENERATED, TEMPLATE_BASED, VOICE_GENERATED)
     * @return List of memes of the specified type
     */
    List<Meme> findByType(Meme.MemeType type);

    /**
     * Find memes created after a certain date
     *
     * @param date Date threshold
     * @return List of memes created after the specified date
     */
    List<Meme> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find memes by user ID, type and created after date
     *
     * @param userId Telegram ID of the user
     * @param type   Type of meme
     * @param date   Date threshold
     * @return List of memes matching all criteria
     */
    List<Meme> findByUserIdAndTypeAndCreatedAtAfter(String userId, Meme.MemeType type, LocalDateTime date);

    /**
     * Find memes in contest ordered by likes (descending)
     * Used for determining contest winners
     *
     * @return List of contest memes sorted by likes (highest first)
     */
    List<Meme> findByInContestIsTrueOrderByLikesDesc();

    /**
     * Count memes currently in contest
     *
     * @return Number of memes in current contest
     */
    int countByInContestIsTrue();
} 