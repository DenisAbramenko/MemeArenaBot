package org.abr.memearenabot.service;

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
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
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
    
    public String getTemplateChooseMessage() {
        return getMessage("command.template.choose");
    }
    
    public String getTemplateTextMessage(String templateName) {
        return getMessage("command.template.text", templateName);
    }
    
    public String getContestInfoMessage() {
        return getMessage("command.contest.info");
    }
    
    public String getNftInfoMessage() {
        return getMessage("command.nft.info");
    }
    
    // Meme generation messages
    public String getMemeGeneratingMessage() {
        return getMessage("meme.generating");
    }
    
    public String getMemeGeneratingAiMessage() {
        return getMessage("meme.generating.ai");
    }
    
    public String getMemeGeneratingTemplateMessage() {
        return getMessage("meme.generating.template");
    }
    
    public String getMemeGeneratingVoiceMessage() {
        return getMessage("meme.generating.voice");
    }
    
    public String getMemeResultAiMessage() {
        return getMessage("meme.result.ai");
    }
    
    public String getMemeResultTemplateMessage() {
        return getMessage("meme.result.template");
    }
    
    public String getMemeResultVoiceMessage() {
        return getMessage("meme.result.voice");
    }
    
    public String getMemeErrorAiMessage() {
        return getMessage("meme.error.ai");
    }
    
    public String getMemeErrorTemplateMessage() {
        return getMessage("meme.error.template");
    }
    
    public String getMemeErrorVoiceMessage() {
        return getMessage("meme.error.voice");
    }
    
    public String getMemeErrorVoiceDownloadMessage() {
        return getMessage("meme.error.voice.download");
    }
    
    public String getMemeErrorAiDisabledMessage() {
        return getMessage("meme.error.ai.disabled");
    }
    
    public String getMemeErrorVoiceDisabledMessage() {
        return getMessage("meme.error.voice.disabled");
    }
    
    public String getMemeErrorDescriptionLongMessage() {
        return getMessage("meme.error.description.long");
    }
    
    public String getMemeActionsMessage() {
        return getMessage("meme.actions");
    }
    
    // Meme action messages
    public String getMemeActionPublishMessage() {
        return getMessage("meme.action.publish");
    }
    
    public String getMemeActionContestMessage() {
        return getMessage("meme.action.contest");
    }
    
    public String getMemeActionNftMessage() {
        return getMessage("meme.action.nft");
    }
    
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
    
    public String getMemeNftSuccessMessage(String nftUrl) {
        return getMessage("meme.nft.success", nftUrl);
    }
    
    public String getMemeNftErrorMessage() {
        return getMessage("meme.nft.error");
    }
    
    // Keyboard button messages
    public String getKeyboardAiMessage() {
        return getMessage("keyboard.ai");
    }
    
    public String getKeyboardTemplateMessage() {
        return getMessage("keyboard.template");
    }
    
    public String getKeyboardVoiceMessage() {
        return getMessage("keyboard.voice");
    }
    
    public String getKeyboardContestMessage() {
        return getMessage("keyboard.contest");
    }
    
    public String getKeyboardMonetizationMessage() {
        return getMessage("keyboard.monetization");
    }
    
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
} 