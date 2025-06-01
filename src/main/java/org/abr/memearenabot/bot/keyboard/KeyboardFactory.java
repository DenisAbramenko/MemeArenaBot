package org.abr.memearenabot.bot.keyboard;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.service.MessageService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating Telegram keyboards
 */
public class KeyboardFactory {
    
    private final TelegramBot bot;
    private final MessageService messageService;
    
    public KeyboardFactory(TelegramBot bot, MessageService messageService) {
        this.bot = bot;
        this.messageService = messageService;
    }
    
    /**
     * Create main menu keyboard
     */
    public ReplyKeyboardMarkup createMainMenuKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(messageService.getKeyboardAiMessage()));
        row1.add(new KeyboardButton(messageService.getKeyboardTemplateMessage()));
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(messageService.getKeyboardVoiceMessage()));
        row2.add(new KeyboardButton(messageService.getKeyboardContestMessage()));
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(messageService.getKeyboardMonetizationMessage()));
        row3.add(new KeyboardButton(messageService.getKeyboardHelpMessage()));
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        
        return replyKeyboardMarkup;
    }
    
    /**
     * Create template selection keyboard
     */
    public ReplyKeyboardMarkup createTemplateKeyboard(List<String> templates) {
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        // Create rows with 2 templates per row
        for (int i = 0; i < templates.size(); i += 2) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(templates.get(i)));
            
            if (i + 1 < templates.size()) {
                row.add(new KeyboardButton(templates.get(i + 1)));
            }
            
            keyboard.add(row);
        }
        
        // Add back button
        KeyboardRow lastRow = new KeyboardRow();
        lastRow.add(new KeyboardButton("↩ Back"));
        keyboard.add(lastRow);
        
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        
        return replyKeyboardMarkup;
    }
    
    /**
     * Create keyboard for voice message request
     */
    public ReplyKeyboardMarkup createVoiceRequestKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("🎙 Record Voice");
        button.setRequestContact(false);
        button.setRequestLocation(false);
        button.setRequestPoll(null);
        
        row.add(button);
        keyboard.add(row);
        
        KeyboardRow backRow = new KeyboardRow();
        backRow.add(new KeyboardButton("↩ Back"));
        keyboard.add(backRow);
        
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        
        return replyKeyboardMarkup;
    }
    
    /**
     * Create meme action keyboard
     */
    public ReplyKeyboardMarkup createMemeActionKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(messageService.getMemeActionPublishMessage()));
        row1.add(new KeyboardButton(messageService.getMemeActionContestMessage()));
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(messageService.getMemeActionNftMessage()));
        row2.add(new KeyboardButton(messageService.getMemeActionNewMessage()));
        
        keyboard.add(row1);
        keyboard.add(row2);
        
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        
        return replyKeyboardMarkup;
    }

    /**
     * Create admin menu keyboard
     */
    public ReplyKeyboardMarkup createAdminMenuKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(messageService.getMessage("admin.button.users")));
        row1.add(new KeyboardButton(messageService.getMessage("admin.button.stats")));
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(messageService.getMessage("admin.button.settings")));
        row2.add(new KeyboardButton(messageService.getMessage("admin.button.broadcast")));
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(messageService.getMessage("admin.button.maintenance")));
        row3.add(new KeyboardButton(messageService.getMessage("admin.contest.end")));
        
        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton(messageService.getMessage("admin.button.back")));
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        
        return replyKeyboardMarkup;
    }

    /**
     * Create user management keyboard
     */
    public ReplyKeyboardMarkup createUserManagementKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(messageService.getMessage("admin.users.list")));
        row1.add(new KeyboardButton(messageService.getMessage("admin.users.search")));
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(messageService.getMessage("admin.users.premium")));
        row2.add(new KeyboardButton(messageService.getMessage("admin.users.block")));
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(messageService.getMessage("admin.users.inactive")));
        row3.add(new KeyboardButton(messageService.getMessage("admin.button.back")));
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        
        return replyKeyboardMarkup;
    }

    /**
     * Create settings keyboard
     */
    public ReplyKeyboardMarkup createSettingsKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(messageService.getMessage("admin.settings.ai")));
        row1.add(new KeyboardButton(messageService.getMessage("admin.settings.voice")));
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(messageService.getMessage("admin.settings.templates")));
        row2.add(new KeyboardButton(messageService.getMessage("admin.settings.contest")));
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(messageService.getMessage("admin.button.back")));
        
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        
        return replyKeyboardMarkup;
    }
} 