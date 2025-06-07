package org.abr.memearenabot.bot.session;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing user session state
 */
public class UserSession {

    private final Map<String, Object> sessionData = new HashMap<>();
    // Getters and setters
    // Current state of the user in the conversation
    @Getter
    private UserState state = UserState.IDLE;
    // Last activity time
    @Getter
    private LocalDateTime lastActivity = LocalDateTime.now();
    // Last meme URL generated for the user
    @Getter
    private String lastMemeUrl;
    // Last command executed by the user
    @Getter
    private String lastCommand;

    /**
     * Update last activity time
     */
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * Reset session state
     */
    public void reset() {
        this.state = UserState.IDLE;
        this.lastCommand = null;
        updateActivity();
    }

    /**
     * Check if session is expired
     */
    public boolean isExpired(int timeoutMinutes) {
        return LocalDateTime.now().isAfter(lastActivity.plusMinutes(timeoutMinutes));
    }

    public void setState(UserState state) {
        this.state = state;
        updateActivity();
    }

    public void setLastMemeUrl(String lastMemeUrl) {
        this.lastMemeUrl = lastMemeUrl;
        updateActivity();
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
        updateActivity();
    }

    /**
     * Set additional data in session
     */
    public void setData(String key, Object value) {
        sessionData.put(key, value);
    }

    /**
     * Get additional data from session
     */
    public Object getData(String key) {
        return sessionData.get(key);
    }

    /**
     * Remove data from session
     */
    public void removeData(String key) {
        sessionData.remove(key);
    }

    /**
     * Clear all session data
     */
    public void clearData() {
        sessionData.clear();
    }
} 