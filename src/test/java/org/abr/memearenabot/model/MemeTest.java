package org.abr.memearenabot.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class MemeTest {

    private Meme meme;
    private User user;
    private static final String IMAGE_URL = "https://example.com/meme.jpg";
    private static final String DESCRIPTION = "Test meme description";
    private static final String USER_ID = "123456789";

    @BeforeEach
    public void setUp() {
        user = new User(USER_ID, "test_user", "Test", "User", "en");
        user.setId(1L);
        
        meme = new Meme(IMAGE_URL, DESCRIPTION, user);
        meme.setId(1L);
    }

    @Test
    public void testConstructorWithUser() {
        assertEquals(IMAGE_URL, meme.getImageUrl());
        assertEquals(DESCRIPTION, meme.getDescription());
        assertEquals(user, meme.getUser());
        assertEquals(USER_ID, meme.getUserId());
        assertEquals(Meme.MemeType.AI_GENERATED, meme.getType());
        assertEquals(0, meme.getLikes());
    }

    @Test
    public void testConstructorWithUserId() {
        Meme memeWithUserId = new Meme(IMAGE_URL, DESCRIPTION, USER_ID);
        
        assertEquals(IMAGE_URL, memeWithUserId.getImageUrl());
        assertEquals(DESCRIPTION, memeWithUserId.getDescription());
        assertEquals(USER_ID, memeWithUserId.getUserId());
        assertEquals(Meme.MemeType.AI_GENERATED, memeWithUserId.getType());
        assertEquals(0, memeWithUserId.getLikes());
    }

    @Test
    public void testConstructorForTemplateBasedMeme() {
        String templateId = "drake";
        Meme templateMeme = new Meme(IMAGE_URL, templateId, user, true);
        
        assertEquals(IMAGE_URL, templateMeme.getImageUrl());
        assertEquals(templateId, templateMeme.getTemplateId());
        assertEquals(user, templateMeme.getUser());
        assertEquals(USER_ID, templateMeme.getUserId());
        assertEquals(Meme.MemeType.TEMPLATE_BASED, templateMeme.getType());
    }

    @Test
    public void testOnCreate() {
        meme.setCreatedAt(null);
        meme.onCreate();
        
        assertNotNull(meme.getCreatedAt());
    }

    @Test
    public void testIncrementLikes() {
        int initialLikes = meme.getLikes();
        meme.incrementLikes();
        
        assertEquals(initialLikes + 1, meme.getLikes());
        
        // Test with null value
        meme.setLikes(null);
        meme.incrementLikes();
        
        assertEquals(1, meme.getLikes());
    }

    @Test
    public void testIncrementLikesWithUser() {
        int initialUserLikes = user.getTotalLikes();
        int initialMemeeLikes = meme.getLikes();
        
        meme.incrementLikes();
        
        assertEquals(initialMemeeLikes + 1, meme.getLikes());
        assertEquals(initialUserLikes + 1, user.getTotalLikes());
    }

    @Test
    public void testSetUser() {
        User newUser = new User("987654321", "new_user", "New", "User", "fr");
        newUser.setId(2L);
        
        meme.setUser(newUser);
        
        assertEquals(newUser, meme.getUser());
        assertEquals(newUser.getTelegramId(), meme.getUserId());
    }

    @Test
    public void testEquals() {
        Meme sameMeme = new Meme(IMAGE_URL, DESCRIPTION, user);
        sameMeme.setId(1L);
        
        Meme differentId = new Meme(IMAGE_URL, DESCRIPTION, user);
        differentId.setId(2L);
        
        // Test equality
        assertEquals(meme, meme); // Same instance
        assertEquals(meme, sameMeme); // Same id
        assertNotEquals(meme, differentId); // Different id
        assertNotEquals(meme, null); // Null comparison
        assertNotEquals(meme, new Object()); // Different type
    }

    @Test
    public void testHashCode() {
        Meme sameMeme = new Meme(IMAGE_URL, DESCRIPTION, user);
        sameMeme.setId(1L);
        
        // Test hash code consistency
        assertEquals(meme.hashCode(), meme.hashCode()); // Same instance, multiple calls
        assertEquals(meme.hashCode(), sameMeme.hashCode()); // Same content
    }

    @Test
    public void testToString() {
        String toString = meme.toString();
        
        // Check that toString contains important fields
        assertTrue(toString.contains(meme.getId().toString()));
        assertTrue(toString.contains(meme.getType().toString()));
        assertTrue(toString.contains(meme.getUserId()));
    }

    @Test
    public void testNullFields() {
        // Test null validation for imageUrl
        Exception exception = assertThrows(NullPointerException.class, () -> {
            new Meme(null, DESCRIPTION, user);
        });
        assertTrue(exception.getMessage().contains("Image URL cannot be null"));
        
        // Test null validation for user
        Exception exception2 = assertThrows(NullPointerException.class, () -> {
            new Meme(IMAGE_URL, DESCRIPTION, (User) null);
        });
        assertTrue(exception2.getMessage().contains("User cannot be null"));
        
        // Test null validation for userId
        Exception exception3 = assertThrows(NullPointerException.class, () -> {
            new Meme(IMAGE_URL, DESCRIPTION, (String) null);
        });
        assertTrue(exception3.getMessage().contains("User ID cannot be null"));
        
        // Test null validation for templateId in template-based constructor
        Exception exception4 = assertThrows(NullPointerException.class, () -> {
            new Meme(IMAGE_URL, null, user, true);
        });
        assertTrue(exception4.getMessage().contains("Template ID cannot be null"));
    }
} 