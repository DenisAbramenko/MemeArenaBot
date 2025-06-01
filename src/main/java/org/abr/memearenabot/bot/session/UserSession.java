package org.abr.memearenabot.bot.session;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing user session state
 */
public class UserSession {

    private final Map<String, Object> sessionData = new HashMap<>();
    // Current state of the user in the conversation
    private UserState state = UserState.IDLE;
    // Last activity time
    private LocalDateTime lastActivity = LocalDateTime.now();
    // Selected template for meme generation
    private String selectedTemplate;
    // Last generated meme URL
    private String lastMemeUrl;
    // Text lines for template-based meme
    private List<String> templateTextLines = new ArrayList<>();
    // Last command executed
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
        this.selectedTemplate = null;
        this.templateTextLines.clear();
        this.lastCommand = null;
        updateActivity();
    }

    /**
     * Check if session is expired
     */
    public boolean isExpired(int timeoutMinutes) {
        return LocalDateTime.now().isAfter(lastActivity.plusMinutes(timeoutMinutes));
    }

    // Getters and setters
    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
        updateActivity();
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public String getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(String selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
        updateActivity();
    }

    public String getLastMemeUrl() {
        return lastMemeUrl;
    }

    public void setLastMemeUrl(String lastMemeUrl) {
        this.lastMemeUrl = lastMemeUrl;
        updateActivity();
    }

    public List<String> getTemplateTextLines() {
        return templateTextLines;
    }

    public void setTemplateTextLines(List<String> templateTextLines) {
        this.templateTextLines = templateTextLines;
        updateActivity();
    }

    public void addTemplateTextLine(String line) {
        this.templateTextLines.add(line);
        updateActivity();
    }

    public void clearTemplateTextLines() {
        this.templateTextLines.clear();
        updateActivity();
    }

    public String getLastCommand() {
        return lastCommand;
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