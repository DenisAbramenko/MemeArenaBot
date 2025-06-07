package org.abr.memearenabot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for localized messages
 */
@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageSource messageSource;

    @Autowired
    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get message by key using default locale
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get message by key with arguments using default locale
     */
    public String getMessage(String key, Object... args) {
        try {
            logger.debug("Requesting message with key: {}", key);
            String message = messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
            logger.debug("Message found for key {}: {}", key, message);
            return message;
        } catch (Exception e) {
            logger.warn("Message not found for key: {}. Error: {}", key, e.getMessage());
            return "!" + key + "!";
        }
    }

    /**
     * Get message by key using specified locale
     */
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * Get message by key with arguments using specified locale
     */
    public String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    // Welcome messages
    public String getWelcomeMessage() {
        return getMessage("welcome.message");
    }

    public String getWelcomeAction() {
        return getMessage("welcome.action");
    }

    // Help messages
    public String getHelpMessage() {
        return getMessage("help.message");
    }

    // Command messages
    public String getUnknownCommandMessage() {
        return getMessage("command.unknown");
    }

    public String getAiPromptMessage() {
        return getMessage("command.ai.prompt");
    }

    public String getContestInfoMessage() {
        return getMessage("command.contest.info");
    }

    /**
     * Get meme generating message
     */
    public String getMemeGeneratingMessage() {
        return getMessage("meme.generating");
    }

    /**
     * Get meme generating AI message
     */
    public String getMemeGeneratingAiMessage() {
        return getMessage("meme.generating.ai");
    }

    /**
     * Get meme result AI message
     */
    public String getMemeResultAiMessage() {
        return getMessage("meme.result.ai");
    }

    /**
     * Get meme error AI message
     */
    public String getMemeErrorAiMessage() {
        return getMessage("meme.error.ai");
    }

    public String getMemeErrorAiDisabledMessage() {
        return getMessage("meme.error.ai.disabled");
    }

    public String getMemeErrorDescriptionLongMessage() {
        return getMessage("meme.error.description.long");
    }

    public String getMemeActionsMessage() {
        return getMessage("meme.actions");
    }

    // Meme action messages
    /**
     * Get meme action publish message
     */
    public String getMemeActionPublishMessage() {
        return getMessage("meme.action.publish");
    }

    /**
     * Get meme action contest message
     */
    public String getMemeActionContestMessage() {
        return getMessage("meme.action.contest");
    }

    /**
     * Get meme action new message
     */
    public String getMemeActionNewMessage() {
        return getMessage("meme.action.new");
    }

    public String getMemePublishSuccessMessage() {
        return getMessage("meme.publish.success");
    }

    public String getMemePublishErrorMessage() {
        return getMessage("meme.publish.error");
    }

    public String getMemeContestSuccessMessage() {
        return getMessage("meme.contest.success");
    }

    public String getMemeContestErrorMessage() {
        return getMessage("meme.contest.error");
    }

    // Keyboard button messages
    /**
     * Get keyboard AI message
     */
    public String getKeyboardAiMessage() {
        return getMessage("keyboard.ai");
    }

    /**
     * Get keyboard contest message
     */
    public String getKeyboardContestMessage() {
        return getMessage("keyboard.contest");
    }

    /**
     * Get keyboard help message
     */
    public String getKeyboardHelpMessage() {
        return getMessage("keyboard.help");
    }

    /**
     * Get admin panel welcome message
     */
    public String getAdminWelcomeMessage() {
        return getMessage("admin.welcome");
    }

    /**
     * Get admin help message
     */
    public String getAdminHelpMessage() {
        return getMessage("admin.help");
    }

    /**
     * Get admin users button message
     */
    public String getAdminUsersButtonMessage() {
        return getMessage("admin.button.users");
    }

    /**
     * Get admin stats button message
     */
    public String getAdminStatsButtonMessage() {
        return getMessage("admin.button.stats");
    }

    /**
     * Get admin settings button message
     */
    public String getAdminSettingsButtonMessage() {
        return getMessage("admin.button.settings");
    }

    /**
     * Get admin broadcast button message
     */
    public String getAdminBroadcastButtonMessage() {
        return getMessage("admin.button.broadcast");
    }

    /**
     * Get admin maintenance button message
     */
    public String getAdminMaintenanceButtonMessage() {
        return getMessage("admin.button.maintenance");
    }

    /**
     * Get admin back button message
     */
    public String getAdminBackButtonMessage() {
        return getMessage("admin.button.back");
    }

    /**
     * Get localized text message
     */
    public String getLocalizedText(String code, Object... args) {
        return getMessage(code, args);
    }

    /**
     * Get meme error AI limit message
     */
    public String getMemeErrorAiLimitMessage() {
        return getMessage("meme.error.ai.limit");
    }
} 