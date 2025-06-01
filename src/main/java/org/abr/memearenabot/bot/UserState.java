package org.abr.memearenabot.bot;

/**
 * Enum representing possible user states in conversation with bot
 */
public enum UserState {
    IDLE,
    AWAITING_AI_DESCRIPTION,
    AWAITING_TEMPLATE_CHOICE,
    AWAITING_TEMPLATE_TEXT
} 