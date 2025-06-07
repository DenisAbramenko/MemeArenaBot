package org.abr.memearenabot.service;

import org.abr.memearenabot.model.Meme;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.repository.MemeRepository;
import org.abr.memearenabot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemeServiceTest {

    @Mock
    private MemeRepository memeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MemeService memeService;

    private User testUser;
    private Meme testMeme;
    private String memeUrl = "https://meme-storage.com/memes/test.jpg";

    @BeforeEach
    public void setUp() {
        testUser = new User("123456789", "test_user", "Test", "User", "en");
        testUser.setId(1L);

        testMeme = new Meme(memeUrl, "Test description", testUser);
        testMeme.setId(1L);
        
        ReflectionTestUtils.setField(memeService, "memeStorageUrl", "https://meme-storage.com/memes/");
        ReflectionTestUtils.setField(memeService, "aiEnabled", true);
        ReflectionTestUtils.setField(memeService, "voiceEnabled", true);
    }

    @Test
    public void testGenerateMeme_Success() {
        String description = "Test description";
        when(memeRepository.save(any(Meme.class))).thenReturn(testMeme);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        CompletableFuture<String> future = memeService.generateMeme(description, testUser);
        
        assertNotNull(future);
        verify(memeRepository).save(any(Meme.class));
        verify(userRepository).save(testUser);
    }

    @Test
    public void testPublishMemeToFeed_MemeFound() {
        when(memeRepository.findAll()).thenReturn(Arrays.asList(testMeme));

        boolean result = memeService.publishMemeToFeed(memeUrl, testUser.getTelegramId());
        
        assertTrue(result);
    }

    @Test
    public void testPublishMemeToFeed_MemeNotFound() {
        when(memeRepository.findAll()).thenReturn(Arrays.asList());

        boolean result = memeService.publishMemeToFeed(memeUrl, testUser.getTelegramId());
        
        assertFalse(result);
    }

    @Test
    public void testSubmitMemeToContest_MemeFound() {
        when(memeRepository.findAll()).thenReturn(Arrays.asList(testMeme));
        when(memeRepository.save(any(Meme.class))).thenReturn(testMeme);

        boolean result = memeService.submitMemeToContest(memeUrl, testUser.getTelegramId());
        
        assertTrue(result);
        verify(memeRepository).save(testMeme);
    }

    @Test
    public void testSubmitMemeToContest_MemeNotFound() {
        when(memeRepository.findAll()).thenReturn(Arrays.asList());

        boolean result = memeService.submitMemeToContest(memeUrl, testUser.getTelegramId());
        
        assertFalse(result);
        verify(memeRepository, never()).save(any(Meme.class));
    }

    @Test
    public void testVoteMeme_MemeFound() {
        when(memeRepository.findById(1L)).thenReturn(Optional.of(testMeme));
        when(memeRepository.save(any(Meme.class))).thenReturn(testMeme);

        boolean result = memeService.voteMeme(1L);
        
        assertTrue(result);
        assertEquals(1, testMeme.getLikes());
        verify(memeRepository).save(testMeme);
    }

    @Test
    public void testVoteMeme_MemeNotFound() {
        when(memeRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = memeService.voteMeme(1L);
        
        assertFalse(result);
        verify(memeRepository, never()).save(any(Meme.class));
    }

    @Test
    public void testGetTopMemes() {
        when(memeRepository.findTop10ByOrderByLikesDesc()).thenReturn(Arrays.asList(testMeme));

        List<Meme> result = memeService.getTopMemes();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMeme, result.get(0));
    }

    @Test
    public void testGetContestMemes() {
        when(memeRepository.findByInContestIsTrue()).thenReturn(Arrays.asList(testMeme));

        List<Meme> result = memeService.getContestMemes();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMeme, result.get(0));
    }

    @Test
    public void testGetMemesByUser() {
        String userId = "123456789";
        when(memeRepository.findByUserId(userId)).thenReturn(Arrays.asList(testMeme));

        List<Meme> result = memeService.getMemesByUser(userId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMeme, result.get(0));
    }
} 