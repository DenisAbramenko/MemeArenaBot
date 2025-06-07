package org.abr.memearenabot.service;

import org.abr.memearenabot.model.Meme;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.repository.MemeRepository;
import org.abr.memearenabot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContestServiceTest {

    private static final String TEST_USER_ID = "12345";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_MEME_URL = "https://example.com/meme.jpg";
    private static final int REQUIRED_PARTICIPANTS = 33;
    
    @Mock
    private MemeRepository memeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageService messageService;

    private ContestService contestService;

    @BeforeEach
    public void setUp() {
        contestService = new ContestService(memeRepository, userRepository, messageService);
    }

    @Test
    public void testGetCurrentContestMemes() {
        // Arrange
        List<Meme> contestMemes = Collections.singletonList(new Meme());
        when(memeRepository.findByInContestIsTrue()).thenReturn(contestMemes);

        // Act
        List<Meme> result = contestService.getCurrentContestMemes();

        // Assert
        assertEquals(contestMemes, result);
        verify(memeRepository).findByInContestIsTrue();
    }

    @Test
    public void testGetCurrentContestParticipantsCount() {
        // Arrange
        int expectedCount = 15;
        when(memeRepository.countByInContestIsTrue()).thenReturn(expectedCount);

        // Act
        int count = contestService.getCurrentContestParticipantsCount();

        // Assert
        assertEquals(expectedCount, count);
        verify(memeRepository).countByInContestIsTrue();
    }

    @Test
    public void testSubmitMemeToContest_Success() {
        // Arrange
        Meme meme = createMeme(TEST_MEME_URL);
        List<Meme> allMemes = Collections.singletonList(meme);
        
        when(memeRepository.findAll()).thenReturn(allMemes);
        when(memeRepository.countByInContestIsTrue()).thenReturn(10); // Not enough for auto-end
        
        // Act
        boolean result = contestService.submitMemeToContest(TEST_MEME_URL, TEST_USER_ID);
        
        // Assert
        assertTrue(result);
        verify(memeRepository).save(meme);
    }

    @Test
    public void testSubmitMemeToContest_AutoEnd() {
        // Arrange
        Meme meme = createMeme(TEST_MEME_URL);
        List<Meme> allMemes = Collections.singletonList(meme);
        List<Meme> contestMemes = Collections.singletonList(meme);
        
        when(memeRepository.findAll()).thenReturn(allMemes);
        when(memeRepository.countByInContestIsTrue()).thenReturn(REQUIRED_PARTICIPANTS); // Enough for auto-end
        when(memeRepository.findByInContestIsTrueOrderByLikesDesc()).thenReturn(contestMemes);
        
        // Act
        boolean result = contestService.submitMemeToContest(TEST_MEME_URL, TEST_USER_ID);
        
        // Assert
        assertTrue(result);
        verify(memeRepository).save(meme);
    }

    @Test
    public void testEndContestAndAwardWinner_Success() {
        // Arrange
        Meme winnerMeme = createContestMeme(TEST_USER_ID, 10);
        Meme otherMeme = createContestMeme("67890", 5);
        
        List<Meme> contestMemes = new ArrayList<>();
        contestMemes.add(winnerMeme);
        contestMemes.add(otherMeme);
        
        User user = createUser(TEST_USER_ID);
        
        when(memeRepository.findByInContestIsTrueOrderByLikesDesc()).thenReturn(contestMemes);
        when(userRepository.findByTelegramId(TEST_USER_ID)).thenReturn(Optional.of(user));
        
        // Act
        boolean result = contestService.endContestAndAwardWinner();
        
        // Assert
        assertTrue(result);
        assertTrue(user.getIsPremium());
        verify(userRepository).save(user);
        verify(memeRepository, times(2)).save(any(Meme.class));
    }

    @Test
    public void testEndContestAndAwardWinner_NoMemes() {
        // Arrange
        when(memeRepository.findByInContestIsTrueOrderByLikesDesc()).thenReturn(Collections.emptyList());
        
        // Act
        boolean result = contestService.endContestAndAwardWinner();
        
        // Assert
        assertFalse(result);
        verify(memeRepository, never()).save(any(Meme.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testAdminEndContest_Success() {
        // Arrange
        String winnerMessage = "Contest ended, winner is @testuser";
        
        Meme winnerMeme = createContestMeme(TEST_USER_ID, 10);
        List<Meme> contestMemes = Collections.singletonList(winnerMeme);
        
        User user = createUser(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        
        when(memeRepository.findByInContestIsTrueOrderByLikesDesc()).thenReturn(contestMemes);
        when(userRepository.findByTelegramId(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(messageService.getMessage(eq("contest.ended.winner"), eq(TEST_USERNAME))).thenReturn(winnerMessage);
        
        // Act
        String result = contestService.adminEndContest();
        
        // Assert
        assertEquals(winnerMessage, result);
        assertTrue(user.getIsPremium());
        verify(userRepository).save(user);
        verify(memeRepository).save(winnerMeme);
    }

    @Test
    public void testAdminEndContest_NoMemes() {
        // Arrange
        String noMemesMessage = "No memes in contest";
        
        when(memeRepository.findByInContestIsTrueOrderByLikesDesc()).thenReturn(Collections.emptyList());
        when(messageService.getMessage("contest.no.memes")).thenReturn(noMemesMessage);
        
        // Act
        String result = contestService.adminEndContest();
        
        // Assert
        assertEquals(noMemesMessage, result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testGetContestStatusMessage_Empty() {
        // Arrange
        String emptyMessage = "No participants";
        
        when(memeRepository.countByInContestIsTrue()).thenReturn(0);
        when(messageService.getMessage("contest.status.empty")).thenReturn(emptyMessage);
        
        // Act
        String result = contestService.getContestStatusMessage();
        
        // Assert
        assertEquals(emptyMessage, result);
    }

    @Test
    public void testGetContestStatusMessage_Progress() {
        // Arrange
        String progressMessage = "15 of 33 participants (18 remaining)";
        int current = 15;
        int remaining = REQUIRED_PARTICIPANTS - current;
        
        when(memeRepository.countByInContestIsTrue()).thenReturn(current);
        when(messageService.getMessage(eq("contest.status.progress"), eq(current), eq(REQUIRED_PARTICIPANTS), eq(remaining)))
            .thenReturn(progressMessage);
        
        // Act
        String result = contestService.getContestStatusMessage();
        
        // Assert
        assertEquals(progressMessage, result);
    }

    @Test
    public void testGetContestStatusMessage_Full() {
        // Arrange
        String fullMessage = "Contest is full";
        
        when(memeRepository.countByInContestIsTrue()).thenReturn(REQUIRED_PARTICIPANTS);
        when(messageService.getMessage("contest.status.full")).thenReturn(fullMessage);
        
        // Act
        String result = contestService.getContestStatusMessage();
        
        // Assert
        assertEquals(fullMessage, result);
    }
    
    // Helper methods
    private Meme createMeme(String imageUrl) {
        Meme meme = new Meme();
        meme.setImageUrl(imageUrl);
        return meme;
    }
    
    private Meme createContestMeme(String userId, int likes) {
        Meme meme = new Meme();
        meme.setUserId(userId);
        meme.setLikes(likes);
        meme.setInContest(true);
        return meme;
    }
    
    private User createUser(String telegramId) {
        User user = new User();
        user.setTelegramId(telegramId);
        return user;
    }
} 