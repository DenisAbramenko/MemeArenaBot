package org.abr.memearenabot.bot.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserSessionTest {

    private UserSession session;

    @BeforeEach
    public void setUp() {
        session = new UserSession();
    }

    @Test
    public void testInitialState() {
        assertEquals(UserState.IDLE, session.getState());
        assertNotNull(session.getLastActivity());
        assertNull(session.getSelectedTemplate());
        assertNull(session.getLastMemeUrl());
        assertTrue(session.getTemplateTextLines().isEmpty());
        assertNull(session.getLastCommand());
    }

    @Test
    public void testUpdateActivity() {
        LocalDateTime initialActivity = session.getLastActivity();
        
        // Sleep a little to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        session.updateActivity();
        
        assertTrue(session.getLastActivity().isAfter(initialActivity));
    }

    @Test
    public void testReset() {
        // Set some values
        session.setState(UserState.WAITING_FOR_AI_DESCRIPTION);
        session.setSelectedTemplate("drake");
        session.addTemplateTextLine("Line 1");
        session.setLastCommand("/template");
        
        // Reset session
        session.reset();
        
        // Check values after reset
        assertEquals(UserState.IDLE, session.getState());
        assertNull(session.getSelectedTemplate());
        assertTrue(session.getTemplateTextLines().isEmpty());
        assertNull(session.getLastCommand());
    }

    @Test
    public void testIsExpired() throws Exception {
        // Set activity time to 31 minutes ago using reflection
        LocalDateTime oldTime = LocalDateTime.now().minusMinutes(31);
        setLastActivityField(session, oldTime);
        
        // Check if expired after 30 minutes
        assertTrue(session.isExpired(30));
        
        // Check if expired after 60 minutes (should not be)
        assertFalse(session.isExpired(60));
        
        // Update activity
        session.updateActivity();
        
        // Check if expired after 30 minutes (should not be after update)
        assertFalse(session.isExpired(30));
    }

    @Test
    public void testSetState() {
        LocalDateTime initialActivity = session.getLastActivity();
        
        // Sleep a little to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        session.setState(UserState.WAITING_FOR_AI_DESCRIPTION);
        
        assertEquals(UserState.WAITING_FOR_AI_DESCRIPTION, session.getState());
        assertTrue(session.getLastActivity().isAfter(initialActivity));
    }

    @Test
    public void testSetSelectedTemplate() {
        LocalDateTime initialActivity = session.getLastActivity();
        
        // Sleep a little to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        session.setSelectedTemplate("drake");
        
        assertEquals("drake", session.getSelectedTemplate());
        assertTrue(session.getLastActivity().isAfter(initialActivity));
    }

    @Test
    public void testSetLastMemeUrl() {
        LocalDateTime initialActivity = session.getLastActivity();
        
        // Sleep a little to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        session.setLastMemeUrl("https://example.com/meme.jpg");
        
        assertEquals("https://example.com/meme.jpg", session.getLastMemeUrl());
        assertTrue(session.getLastActivity().isAfter(initialActivity));
    }

    @Test
    public void testTemplateTextLinesOperations() {
        // Test add line
        session.addTemplateTextLine("Line 1");
        assertEquals(1, session.getTemplateTextLines().size());
        assertEquals("Line 1", session.getTemplateTextLines().get(0));
        
        // Test add another line
        session.addTemplateTextLine("Line 2");
        assertEquals(2, session.getTemplateTextLines().size());
        assertEquals("Line 2", session.getTemplateTextLines().get(1));
        
        // Test set lines
        List<String> newLines = Arrays.asList("New Line 1", "New Line 2", "New Line 3");
        session.setTemplateTextLines(newLines);
        assertEquals(3, session.getTemplateTextLines().size());
        assertEquals("New Line 1", session.getTemplateTextLines().get(0));
        assertEquals("New Line 3", session.getTemplateTextLines().get(2));
        
        // Test clear lines
        session.clearTemplateTextLines();
        assertTrue(session.getTemplateTextLines().isEmpty());
    }

    @Test
    public void testSetLastCommand() {
        LocalDateTime initialActivity = session.getLastActivity();
        
        // Sleep a little to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        session.setLastCommand("/help");
        
        assertEquals("/help", session.getLastCommand());
        assertTrue(session.getLastActivity().isAfter(initialActivity));
    }
    
    /**
     * Helper method to set the lastActivity field using reflection
     */
    private void setLastActivityField(UserSession session, LocalDateTime value) throws Exception {
        Field field = UserSession.class.getDeclaredField("lastActivity");
        field.setAccessible(true);
        field.set(session, value);
    }
} 