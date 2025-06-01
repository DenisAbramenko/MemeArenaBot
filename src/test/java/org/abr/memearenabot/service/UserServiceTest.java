package org.abr.memearenabot.service;

import org.abr.memearenabot.model.User;
import org.abr.memearenabot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private String telegramId = "123456789";

    @BeforeEach
    public void setUp() {
        testUser = new User(telegramId, "test_user", "Test", "User", "en");
        testUser.setId(1L);
    }

    @Test
    public void testGetOrCreateUser_UserExists() {
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.getOrCreateUser(telegramId, "test_user", "Test", "User", "en");

        assertNotNull(result);
        assertEquals(telegramId, result.getTelegramId());
        assertEquals("test_user", result.getUsername());
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testGetOrCreateUser_UserDoesNotExist() {
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.getOrCreateUser(telegramId, "test_user", "Test", "User", "en");

        assertNotNull(result);
        assertEquals(telegramId, result.getTelegramId());
        assertEquals("test_user", result.getUsername());
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testGetUserByTelegramId() {
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByTelegramId(telegramId);

        assertTrue(result.isPresent());
        assertEquals(telegramId, result.get().getTelegramId());
        verify(userRepository).findByTelegramId(telegramId);
    }

    @Test
    public void testGetUserByUsername() {
        String username = "test_user";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    public void testSetPremiumStatus_UserExists() {
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(testUser));

        boolean result = userService.setPremiumStatus(telegramId, true);

        assertTrue(result);
        assertTrue(testUser.getIsPremium());
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository).save(testUser);
    }

    @Test
    public void testSetPremiumStatus_UserDoesNotExist() {
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());

        boolean result = userService.setPremiumStatus(telegramId, true);

        assertFalse(result);
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testGetTopUsersByMemes() {
        List<User> userList = Arrays.asList(testUser);
        when(userRepository.findTop10ByOrderByTotalMemesDesc()).thenReturn(userList);

        List<User> result = userService.getTopUsersByMemes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        verify(userRepository).findTop10ByOrderByTotalMemesDesc();
    }

    @Test
    public void testGetTopUsersByLikes() {
        List<User> userList = Arrays.asList(testUser);
        when(userRepository.findTop10ByOrderByTotalLikesDesc()).thenReturn(userList);

        List<User> result = userService.getTopUsersByLikes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        verify(userRepository).findTop10ByOrderByTotalLikesDesc();
    }

    @Test
    public void testGetInactiveUsers() {
        int days = 30;
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<User> userList = Arrays.asList(testUser);
        when(userRepository.findByLastActivityBefore(any(LocalDateTime.class))).thenReturn(userList);

        List<User> result = userService.getInactiveUsers(days);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        verify(userRepository).findByLastActivityBefore(any(LocalDateTime.class));
    }

    @Test
    public void testDeleteUser_UserExists() {
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(testUser));

        boolean result = userService.deleteUser(telegramId);

        assertTrue(result);
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository).delete(testUser);
    }

    @Test
    public void testDeleteUser_UserDoesNotExist() {
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());

        boolean result = userService.deleteUser(telegramId);

        assertFalse(result);
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository, never()).delete(any(User.class));
    }
} 