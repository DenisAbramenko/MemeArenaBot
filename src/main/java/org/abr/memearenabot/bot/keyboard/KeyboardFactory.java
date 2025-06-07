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

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(messageService.getKeyboardContestMessage()));

        KeyboardRow row3 = new KeyboardRow();
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
     * Create meme action keyboard
     */
    public ReplyKeyboardMarkup createMemeActionKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(messageService.getMemeActionPublishMessage()));
        row1.add(new KeyboardButton(messageService.getMemeActionContestMessage()));

        KeyboardRow row2 = new KeyboardRow();
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
        row1.add(new KeyboardButton(messageService.getMessage("admin.settings.contest")));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(messageService.getMessage("admin.button.back")));

        keyboard.add(row1);
        keyboard.add(row2);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
    }

    /**
     * Create login keyboard with only user and admin login buttons
     */
    public ReplyKeyboardMarkup createLoginKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(messageService.getMessage("Войти как пользователь")));
        row1.add(new KeyboardButton(messageService.getMessage("Войти как админ")));

        keyboard.add(row1);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
    }
} 