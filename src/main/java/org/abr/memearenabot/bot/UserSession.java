package org.abr.memearenabot.bot;

import java.time.LocalDateTime;

/**
 * Class to track user session state and data
 */
public class UserSession {
    private UserState state = UserState.IDLE;
    private String selectedTemplate;
    private String lastGeneratedMeme;
    private LocalDateTime lastActivity;
    
    public UserSession() {
        this.lastActivity = LocalDateTime.now();
    }
    
    public UserState getState() {
        return state;
    }
    
    public void setState(UserState state) {
        this.state = state;
        updateLastActivity();
    }
    
    public String getSelectedTemplate() {
        return selectedTemplate;
    }
    
    public void setSelectedTemplate(String selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
        updateLastActivity();
    }
    
    public String getLastGeneratedMeme() {
        return lastGeneratedMeme;
    }
    
    public void setLastGeneratedMeme(String lastGeneratedMeme) {
        this.lastGeneratedMeme = lastGeneratedMeme;
        updateLastActivity();
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    private void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Check if session is expired (inactive for more than 30 minutes)
     */
    public boolean isExpired() {
        return LocalDateTime.now().minusMinutes(30).isAfter(lastActivity);
    }
} 