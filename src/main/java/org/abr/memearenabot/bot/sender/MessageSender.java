package org.abr.memearenabot.bot.sender;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;

/**
 * Utility class for sending messages to Telegram
 */
public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final TelegramBot bot;
    private final MessageService messageService;

    public MessageSender(TelegramBot bot, MessageService messageService) {
        this.bot = bot;
        this.messageService = messageService;
    }

    /**
     * Get the bot instance
     */
    public TelegramBot getBot() {
        return bot;
    }

    /**
     * Send text message to chat
     */
    public Message sendText(Long chatId, String text) {
        return sendText(chatId, text, null);
    }

    /**
     * Send text message with keyboard to chat
     */
    public Message sendText(Long chatId, String text, ReplyKeyboard keyboard) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            message.enableHtml(true);

            if (keyboard != null) {
                message.setReplyMarkup(keyboard);
            }

            Message sentMessage = bot.execute(message);
            logger.debug("Sent text message to chat ID: {}", chatId);
            return sentMessage;
        } catch (TelegramApiException e) {
            logger.error("Failed to send text message to chat ID: {}", chatId, e);
            return null;
        }
    }

    /**
     * Send localized message to chat
     */
    public Message sendLocalizedText(Long chatId, String messageKey) {
        return sendText(chatId, messageService.getMessage(messageKey));
    }

    /**
     * Send localized message with arguments to chat
     */
    public Message sendLocalizedText(Long chatId, String messageKey, Object... args) {
        return sendText(chatId, messageService.getMessage(messageKey, args));
    }

    /**
     * Send localized message with keyboard to chat
     */
    public Message sendLocalizedText(Long chatId, String messageKey, ReplyKeyboard keyboard) {
        return sendText(chatId, messageService.getMessage(messageKey), keyboard);
    }

    /**
     * Send localized message with arguments and keyboard to chat
     */
    public Message sendLocalizedText(Long chatId, String messageKey, ReplyKeyboard keyboard, Object... args) {
        return sendText(chatId, messageService.getMessage(messageKey, args), keyboard);
    }

    /**
     * Send photo to chat
     */
    public Message sendPhoto(Long chatId, String photoUrl, String caption) {
        return sendPhoto(chatId, photoUrl, caption, null);
    }

    /**
     * Send photo with keyboard to chat
     */
    public Message sendPhoto(Long chatId, String photoUrl, String caption, ReplyKeyboard keyboard) {
        try {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId);
            photo.setPhoto(new InputFile(photoUrl));

            if (caption != null && !caption.isEmpty()) {
                photo.setCaption(caption);
            }

            if (keyboard != null) {
                photo.setReplyMarkup(keyboard);
            }

            Message sentMessage = bot.execute(photo);
            logger.debug("Sent photo to chat ID: {}", chatId);
            return sentMessage;
        } catch (TelegramApiException e) {
            logger.error("Failed to send photo to chat ID: {}", chatId, e);
            return null;
        }
    }

    /**
     * Send photo from input stream to chat
     */
    public Message sendPhoto(Long chatId, InputStream photoStream, String caption) {
        return sendPhoto(chatId, photoStream, caption, null);
    }

    /**
     * Send photo from input stream with keyboard to chat
     */
    public Message sendPhoto(Long chatId, InputStream photoStream, String caption, ReplyKeyboard keyboard) {
        try {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId);
            photo.setPhoto(new InputFile(photoStream, "meme.jpg"));

            if (caption != null && !caption.isEmpty()) {
                photo.setCaption(caption);
            }

            if (keyboard != null) {
                photo.setReplyMarkup(keyboard);
            }

            Message sentMessage = bot.execute(photo);
            logger.debug("Sent photo from stream to chat ID: {}", chatId);
            return sentMessage;
        } catch (TelegramApiException e) {
            logger.error("Failed to send photo from stream to chat ID: {}", chatId, e);
            return null;
        }
    }

    /**
     * Send photo with localized caption to chat
     */
    public Message sendPhotoWithLocalizedCaption(Long chatId, String photoUrl, String captionKey) {
        return sendPhoto(chatId, photoUrl, messageService.getMessage(captionKey));
    }

    /**
     * Send photo with localized caption and arguments to chat
     */
    public Message sendPhotoWithLocalizedCaption(Long chatId, String photoUrl, String captionKey, Object... args) {
        return sendPhoto(chatId, photoUrl, messageService.getMessage(captionKey, args));
    }

    /**
     * Send photo with localized caption and keyboard to chat
     */
    public Message sendPhotoWithLocalizedCaption(Long chatId, String photoUrl, String captionKey,
                                                 ReplyKeyboard keyboard) {
        return sendPhoto(chatId, photoUrl, messageService.getMessage(captionKey), keyboard);
    }

    /**
     * Send photo with localized caption, arguments, and keyboard to chat
     */
    public Message sendPhotoWithLocalizedCaption(Long chatId, String photoUrl, String captionKey,
                                                 ReplyKeyboard keyboard, Object... args) {
        return sendPhoto(chatId, photoUrl, messageService.getMessage(captionKey, args), keyboard);
    }
} 