package org.abr.memearenabot.bot.session;

/**
 * Enum representing user session states
 */
public enum UserState {
    // Initial state, waiting for user input
    IDLE,
    
    // Waiting for AI description
    WAITING_FOR_AI_DESCRIPTION,
    
    // Waiting for template selection
    WAITING_FOR_TEMPLATE_SELECTION,
    
    // Waiting for template text
    WAITING_FOR_TEMPLATE_TEXT,
    
    // Meme generated, waiting for action
    MEME_GENERATED,
    
    // Admin states
    ADMIN_MENU,
    ADMIN_USERS_MENU,
    ADMIN_SETTINGS_MENU,
    ADMIN_BROADCAST_COMPOSE,
    ADMIN_USER_SEARCH,
    ADMIN_USER_DETAIL,
    ADMIN_TEMPLATE_MANAGEMENT
} 