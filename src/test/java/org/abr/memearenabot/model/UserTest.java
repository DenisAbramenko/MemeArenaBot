package org.abr.memearenabot.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;
    private static final String TELEGRAM_ID = "123456789";
    private static final String USERNAME = "test_user";
    private static final String FIRST_NAME = "Test";
    private static final String LAST_NAME = "User";
    private static final String LANGUAGE_CODE = "en";

    @BeforeEach
    public void setUp() {
        user = new User(TELEGRAM_ID, USERNAME, FIRST_NAME, LAST_NAME, LANGUAGE_CODE);
        user.setId(1L);
    }

    @Test
    public void testConstructor() {
        assertEquals(TELEGRAM_ID, user.getTelegramId());
        assertEquals(USERNAME, user.getUsername());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(LANGUAGE_CODE, user.getLanguageCode());
        assertEquals(0, user.getTotalMemes());
        assertEquals(0, user.getTotalLikes());
        assertFalse(user.getIsPremium());
    }

    @Test
    public void testOnCreate() {
        user.setCreatedAt(null);
        user.setLastActivity(null);
        user.onCreate();
        
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastActivity());
    }

    @Test
    public void testOnUpdate() {
        LocalDateTime oldLastActivity = LocalDateTime.now().minusDays(1);
        user.setLastActivity(oldLastActivity);
        user.onUpdate();
        
        assertTrue(user.getLastActivity().isAfter(oldLastActivity));
    }

    @Test
    public void testUpdateActivity() {
        LocalDateTime oldLastActivity = LocalDateTime.now().minusDays(1);
        user.setLastActivity(oldLastActivity);
        user.updateActivity();
        
        assertTrue(user.getLastActivity().isAfter(oldLastActivity));
    }

    @Test
    public void testIncrementMemes() {
        int initialMemes = user.getTotalMemes();
        user.incrementMemes();
        
        assertEquals(initialMemes + 1, user.getTotalMemes());
        
        // Test with null value
        user.setTotalMemes(null);
        user.incrementMemes();
        
        assertEquals(1, user.getTotalMemes());
    }

    @Test
    public void testIncrementLikes() {
        int initialLikes = user.getTotalLikes();
        user.incrementLikes();
        
        assertEquals(initialLikes + 1, user.getTotalLikes());
        
        // Test with null value
        user.setTotalLikes(null);
        user.incrementLikes();
        
        assertEquals(1, user.getTotalLikes());
    }

    @Test
    public void testAddMeme() {
        Meme meme = new Meme("https://example.com/meme.jpg", "Test meme", "123456789");
        int initialMemes = user.getTotalMemes();
        
        user.addMeme(meme);
        
        assertEquals(initialMemes + 1, user.getTotalMemes());
        assertEquals(user, meme.getUser());
        assertTrue(user.getMemes().contains(meme));
    }

    @Test
    public void testEquals() {
        User sameUser = new User(TELEGRAM_ID, USERNAME, FIRST_NAME, LAST_NAME, LANGUAGE_CODE);
        sameUser.setId(1L);
        
        User differentIdSameTelegramId = new User(TELEGRAM_ID, USERNAME, FIRST_NAME, LAST_NAME, LANGUAGE_CODE);
        differentIdSameTelegramId.setId(2L);
        
        User differentTelegramId = new User("987654321", USERNAME, FIRST_NAME, LAST_NAME, LANGUAGE_CODE);
        differentTelegramId.setId(3L);
        
        // Test equality
        assertEquals(user, user); // Same instance
        assertEquals(user, sameUser); // Same id
        assertEquals(user, differentIdSameTelegramId); // Same telegramId
        assertNotEquals(user, differentTelegramId); // Different id and telegramId
        assertNotEquals(user, null); // Null comparison
        assertNotEquals(user, new Object()); // Different type
    }

    @Test
    public void testHashCode() {
        User sameUser = new User(TELEGRAM_ID, USERNAME, FIRST_NAME, LAST_NAME, LANGUAGE_CODE);
        sameUser.setId(1L);
        
        User differentIdSameTelegramId = new User(TELEGRAM_ID, USERNAME, FIRST_NAME, LAST_NAME, LANGUAGE_CODE);
        differentIdSameTelegramId.setId(2L);
        
        // Test hash code consistency
        assertEquals(user.hashCode(), user.hashCode()); // Same instance, multiple calls
        assertEquals(user.hashCode(), sameUser.hashCode()); // Same content
        // Note: We can't guarantee that different objects have different hash codes
    }

    @Test
    public void testToString() {
        String toString = user.toString();
        
        // Check that toString contains important fields
        assertTrue(toString.contains(TELEGRAM_ID));
        assertTrue(toString.contains(USERNAME));
        assertTrue(toString.contains(FIRST_NAME));
        assertTrue(toString.contains(LAST_NAME));
    }

    @Test
    public void testNullFields() {
        // Test null validation for telegramId
        Exception exception = assertThrows(NullPointerException.class, () -> {
            new User(null, USERNAME, FIRST_NAME, LAST_NAME, LANGUAGE_CODE);
        });
        assertTrue(exception.getMessage().contains("Telegram ID cannot be null"));
        
        // Test null validation for createdAt
        Exception exception2 = assertThrows(NullPointerException.class, () -> {
            user.setCreatedAt(null);
        });
        assertTrue(exception2.getMessage().contains("Creation date cannot be null"));
        
        // Test null validation for lastActivity
        Exception exception3 = assertThrows(NullPointerException.class, () -> {
            user.setLastActivity(null);
        });
        assertTrue(exception3.getMessage().contains("Last activity date cannot be null"));
    }
} 