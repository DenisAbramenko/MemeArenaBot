package org.abr.memearenabot.bot;

import org.abr.memearenabot.bot.handler.CallbackHandler;
import org.abr.memearenabot.bot.handler.CommandHandler;
import org.abr.memearenabot.bot.handler.MessageHandler;
import org.abr.memearenabot.bot.keyboard.InlineKeyboardFactory;
import org.abr.memearenabot.bot.keyboard.KeyboardFactory;
import org.abr.memearenabot.bot.sender.MessageSender;
import org.abr.memearenabot.bot.session.UserSession;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.service.MemeService;
import org.abr.memearenabot.service.MessageService;
import org.abr.memearenabot.service.UserService;
import org.abr.memearenabot.validation.InputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramBotTest {

    @Mock
    private MemeService memeService;

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

    @Mock
    private InputValidator inputValidator;

    @Mock
    private CommandHandler commandHandler;

    @Mock
    private MessageHandler messageHandler;

    @Mock
    private CallbackHandler callbackHandler;

    @Mock
    private KeyboardFactory keyboardFactory;

    @Mock
    private InlineKeyboardFactory inlineKeyboardFactory;

    @Mock
    private MessageSender messageSender;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private CallbackQuery callbackQuery;

    @Mock
    private MaybeInaccessibleMessage maybeMessage;

    @Mock
    private Voice voice;

    @Mock
    private Chat chat;

    @Mock
    private org.telegram.telegrambots.meta.api.objects.User telegramUser;

    private TelegramBot bot;
    private User user;
    private final Long chatId = 123456789L;
    private final String text = "Generate a funny cat meme";

    @BeforeEach
    public void setUp() {
        bot = spy(new TelegramBot(
                "test_token",
                memeService,
                userService,
                messageService,
                inputValidator
        ));

        // Mock dependencies that are created in the constructor
        setPrivateField(bot, "commandHandler", commandHandler);
        setPrivateField(bot, "messageHandler", messageHandler);
        setPrivateField(bot, "callbackHandler", callbackHandler);
        setPrivateField(bot, "keyboardFactory", keyboardFactory);
        setPrivateField(bot, "inlineKeyboardFactory", inlineKeyboardFactory);
        setPrivateField(bot, "messageSender", messageSender);

        user = new User(chatId.toString(), "test_user", "Test", "User", "en");
        user.setId(1L);

        when(message.getChatId()).thenReturn(chatId);
        when(message.getChat()).thenReturn(chat);
    }

    @Test
    public void testOnUpdateReceived_WithMessage() {
        // Arrange
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(userService.getOrCreateUser(message)).thenReturn(user);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).getOrCreateUser(message);
        verify(userService).updateUserActivity(user.getTelegramId());
    }

    @Test
    public void testOnUpdateReceived_WithTextMessage() {
        // Arrange
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(text);
        when(userService.getOrCreateUser(message)).thenReturn(user);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).getOrCreateUser(message);
        verify(userService).updateUserActivity(user.getTelegramId());
        
        // Verify command handling if text starts with "/"
        when(message.getText()).thenReturn("/start");
        bot.onUpdateReceived(update);
        verify(commandHandler).handleCommand(eq(message), eq("/start"), any(UserSession.class), eq(user));
        
        // Verify text message handling for regular text
        when(message.getText()).thenReturn(text);
        bot.onUpdateReceived(update);
        verify(messageHandler).handleTextMessage(eq(message), eq(text), any(UserSession.class), eq(user));
    }

    @Test
    public void testOnUpdateReceived_WithVoiceMessage() {
        // Arrange
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);
        when(message.hasVoice()).thenReturn(true);
        when(message.getVoice()).thenReturn(voice);
        when(userService.getOrCreateUser(message)).thenReturn(user);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).getOrCreateUser(message);
        verify(userService).updateUserActivity(user.getTelegramId());
        verify(messageHandler).handleVoiceMessage(message, bot.getUserSessions().get(chatId), user);
    }

    @Test
    public void testOnUpdateReceived_WithCallbackQuery_MessageAccessible() {
        // Arrange
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.hasMessage()).thenReturn(false);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(userService.getOrCreateUser(message)).thenReturn(user);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).getOrCreateUser(message);
        verify(userService).updateUserActivity(user.getTelegramId());
        verify(callbackHandler).handleCallback(callbackQuery, bot.getUserSessions().get(chatId), user);
    }

    @Test
    public void testOnUpdateReceived_WithCallbackQuery_MessageInaccessible() {
        // Arrange
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.hasMessage()).thenReturn(false);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getMessage()).thenReturn(maybeMessage);
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(telegramUser.getId()).thenReturn(123456789L);
        when(telegramUser.getUserName()).thenReturn("test_user");
        when(telegramUser.getFirstName()).thenReturn("Test");
        when(telegramUser.getLastName()).thenReturn("User");
        when(telegramUser.getLanguageCode()).thenReturn("en");
        when(maybeMessage.getChatId()).thenReturn(chatId);
        
        when(userService.getOrCreateUser(
                eq(telegramUser.getId().toString()),
                eq(telegramUser.getUserName()),
                eq(telegramUser.getFirstName()),
                eq(telegramUser.getLastName()),
                eq(telegramUser.getLanguageCode())
        )).thenReturn(user);

        // Act
        bot.onUpdateReceived(update);

        // Assert
        verify(userService).getOrCreateUser(
                telegramUser.getId().toString(),
                telegramUser.getUserName(),
                telegramUser.getFirstName(),
                telegramUser.getLastName(),
                telegramUser.getLanguageCode()
        );
        verify(userService).updateUserActivity(user.getTelegramId());
        verify(callbackHandler).handleCallback(callbackQuery, bot.getUserSessions().get(chatId), user);
    }

    @Test
    public void testGetUserSessions() {
        // Act
        Map<Long, UserSession> sessions = bot.getUserSessions();
        
        // Assert
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    @Test
    public void testGetters() {
        // Act & Assert
        assertEquals(memeService, bot.getMemeService());
        assertEquals(userService, bot.getUserService());
        assertEquals(messageService, bot.getMessageService());
        assertEquals(inputValidator, bot.getInputValidator());
    }

    @Test
    public void testExceptionHandling() {
        // Arrange
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(userService.getOrCreateUser(message)).thenThrow(new RuntimeException("Test exception"));

        // Act
        bot.onUpdateReceived(update);

        // Assert - No exception should be thrown outside the method
        verify(userService).getOrCreateUser(message);
    }

    /**
     * Helper method to set private fields using reflection
     */
    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private field", e);
        }
    }
} 