package org.abr.memearenabot.bot.sender;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageSenderTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private MessageService messageService;

    @Mock
    private Message mockMessage;

    @Mock
    private ReplyKeyboardMarkup mockKeyboard;

    private MessageSender messageSender;
    private final Long chatId = 123456789L;
    private final String text = "Test message";
    private final String localizedText = "Localized test message";

    @BeforeEach
    public void setUp() {
        messageSender = new MessageSender(bot, messageService);
    }

    @Test
    public void testGetBot() {
        assertEquals(bot, messageSender.getBot());
    }

    @Test
    public void testSendText() throws TelegramApiException {
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        Message result = messageSender.sendText(chatId, text);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    public void testSendTextWithKeyboard() throws TelegramApiException {
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        Message result = messageSender.sendText(chatId, text, mockKeyboard);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    public void testSendTextException() throws TelegramApiException {
        when(bot.execute(any(SendMessage.class))).thenThrow(new TelegramApiException("Test exception"));

        Message result = messageSender.sendText(chatId, text);

        assertNull(result);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    public void testSendLocalizedText() throws TelegramApiException {
        String messageKey = "test.key";
        when(messageService.getMessage(messageKey)).thenReturn(localizedText);
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        Message result = messageSender.sendLocalizedText(chatId, messageKey);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(messageService).getMessage(messageKey);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    public void testSendLocalizedTextWithArgs() throws TelegramApiException {
        String messageKey = "test.key";
        Object[] args = new Object[]{"arg1", 2};
        when(messageService.getMessage(messageKey, args)).thenReturn(localizedText);
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        Message result = messageSender.sendLocalizedText(chatId, messageKey, args);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(messageService).getMessage(messageKey, args);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    public void testSendLocalizedTextWithKeyboard() throws TelegramApiException {
        String messageKey = "test.key";
        when(messageService.getMessage(messageKey)).thenReturn(localizedText);
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        Message result = messageSender.sendLocalizedText(chatId, messageKey, mockKeyboard);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(messageService).getMessage(messageKey);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    public void testSendPhoto() throws TelegramApiException {
        String photoUrl = "https://example.com/photo.jpg";
        String caption = "Photo caption";
        when(bot.execute(any(SendPhoto.class))).thenReturn(mockMessage);

        Message result = messageSender.sendPhoto(chatId, photoUrl, caption);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(bot).execute(any(SendPhoto.class));
    }

    @Test
    public void testSendPhotoWithKeyboard() throws TelegramApiException {
        String photoUrl = "https://example.com/photo.jpg";
        String caption = "Photo caption";
        when(bot.execute(any(SendPhoto.class))).thenReturn(mockMessage);

        Message result = messageSender.sendPhoto(chatId, photoUrl, caption, mockKeyboard);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(bot).execute(any(SendPhoto.class));
    }

    @Test
    public void testSendPhotoException() throws TelegramApiException {
        String photoUrl = "https://example.com/photo.jpg";
        String caption = "Photo caption";
        when(bot.execute(any(SendPhoto.class))).thenThrow(new TelegramApiException("Test exception"));

        Message result = messageSender.sendPhoto(chatId, photoUrl, caption);

        assertNull(result);
        verify(bot).execute(any(SendPhoto.class));
    }

    @Test
    public void testSendPhotoFromStream() throws TelegramApiException {
        byte[] photoData = "test photo data".getBytes();
        InputStream photoStream = new ByteArrayInputStream(photoData);
        String caption = "Photo caption";
        when(bot.execute(any(SendPhoto.class))).thenReturn(mockMessage);

        Message result = messageSender.sendPhoto(chatId, photoStream, caption);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(bot).execute(any(SendPhoto.class));
    }

    @Test
    public void testSendPhotoWithLocalizedCaption() throws TelegramApiException {
        String photoUrl = "https://example.com/photo.jpg";
        String captionKey = "photo.caption";
        String localizedCaption = "Localized photo caption";
        when(messageService.getMessage(captionKey)).thenReturn(localizedCaption);
        when(bot.execute(any(SendPhoto.class))).thenReturn(mockMessage);

        Message result = messageSender.sendPhotoWithLocalizedCaption(chatId, photoUrl, captionKey);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(messageService).getMessage(captionKey);
        verify(bot).execute(any(SendPhoto.class));
    }

    @Test
    public void testSendPhotoWithLocalizedCaptionAndArgs() throws TelegramApiException {
        String photoUrl = "https://example.com/photo.jpg";
        String captionKey = "photo.caption";
        Object[] args = new Object[]{"arg1", 2};
        String localizedCaption = "Localized photo caption with args: arg1, 2";
        when(messageService.getMessage(captionKey, args)).thenReturn(localizedCaption);
        when(bot.execute(any(SendPhoto.class))).thenReturn(mockMessage);

        Message result = messageSender.sendPhotoWithLocalizedCaption(chatId, photoUrl, captionKey, args);

        assertNotNull(result);
        assertEquals(mockMessage, result);
        
        verify(messageService).getMessage(captionKey, args);
        verify(bot).execute(any(SendPhoto.class));
    }
} 