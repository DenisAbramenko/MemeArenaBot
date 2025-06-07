package org.abr.memearenabot.bot.session;

/**
 * Enum representing possible user states in the conversation
 */
public enum UserState {
    // Initial state
    IDLE,

    // Login state
    WAITING_FOR_LOGIN,

    // Admin login state - waiting for password
    WAITING_FOR_ADMIN_PASSWORD,

    // Waiting for AI description
    WAITING_FOR_AI_DESCRIPTION,

    // Meme has been generated
    MEME_GENERATED,

    // Admin menu state
    ADMIN_MENU,

    // Admin users menu state
    ADMIN_USERS_MENU,

    // Admin settings menu state
    ADMIN_SETTINGS_MENU,

    // Admin broadcast compose state
    ADMIN_BROADCAST_COMPOSE,

    // Admin user search state
    ADMIN_USER_SEARCH,

    // Admin user detail state
    ADMIN_USER_DETAIL
} 