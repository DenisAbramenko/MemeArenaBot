package org.abr.memearenabot.bot.handler;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.bot.keyboard.KeyboardFactory;
import org.abr.memearenabot.bot.session.UserSession;
import org.abr.memearenabot.bot.session.UserState;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.service.MemeService;
import org.abr.memearenabot.service.MessageService;
import org.abr.memearenabot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandHandlerTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private MemeService memeService;

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

    @Mock
    private KeyboardFactory keyboardFactory;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private ReplyKeyboardMarkup mockKeyboard;

    private CommandHandler commandHandler;
    private UserSession session;
    private User user;
    private final Long chatId = 123456789L;

    @BeforeEach
    public void setUp() {
        commandHandler = new CommandHandler(bot, memeService, userService, messageService, keyboardFactory);
        session = new UserSession();
        user = new User(chatId.toString(), "test_user", "Test", "User", "en");
        user.setId(1L);

        when(message.getChatId()).thenReturn(chatId);
        when(message.getChat()).thenReturn(chat);
    }

    @Test
    public void testHandleStartCommand() throws TelegramApiException {
        // Arrange
        String welcomeMessage = "Welcome to Meme Arena Bot!";
        when(messageService.getWelcomeMessage()).thenReturn(welcomeMessage);
        when(keyboardFactory.createMainMenuKeyboard()).thenReturn(mockKeyboard);

        // Act
        commandHandler.handleCommand(message, "/start", session, user);

        // Assert
        assertEquals(UserState.IDLE, session.getState());
        verify(messageService).getWelcomeMessage();
        verify(keyboardFactory).createMainMenuKeyboard();
        verify(bot).execute(any(SendMessage.class));
        assertNull(session.getLastMemeUrl());
        assertNull(session.getSelectedTemplate());
        assertTrue(session.getTemplateTextLines().isEmpty());
        assertEquals("/start", session.getLastCommand());
    }

    @Test
    public void testHandleHelpCommand() throws TelegramApiException {
        // Arrange
        String helpMessage = "Here's how to use the bot...";
        when(messageService.getHelpMessage()).thenReturn(helpMessage);
        when(keyboardFactory.createMainMenuKeyboard()).thenReturn(mockKeyboard);

        // Act
        commandHandler.handleCommand(message, "/help", session, user);

        // Assert
        verify(messageService).getHelpMessage();
        verify(keyboardFactory).createMainMenuKeyboard();
        verify(bot).execute(any(SendMessage.class));
        assertEquals("/help", session.getLastCommand());
    }

    @Test
    public void testHandleAiCommand() throws TelegramApiException {
        // Arrange
        String aiPromptMessage = "Describe the meme you want to generate";
        when(messageService.getAiPromptMessage()).thenReturn(aiPromptMessage);

        // Act
        commandHandler.handleCommand(message, "/ai", session, user);

        // Assert
        assertEquals(UserState.WAITING_FOR_AI_DESCRIPTION, session.getState());
        verify(messageService).getAiPromptMessage();
        verify(bot).execute(any(SendMessage.class));
        assertEquals("/ai", session.getLastCommand());
    }

    @Test
    public void testHandleTemplateCommand() throws TelegramApiException {
        // Arrange
        String templateChooseMessage = "Choose a template";
        List<String> templates = Arrays.asList("drake", "distracted", "button");
        
        when(messageService.getTemplateChooseMessage()).thenReturn(templateChooseMessage);
        when(memeService.getAvailableTemplates()).thenReturn(templates);
        when(keyboardFactory.createTemplateKeyboard(templates)).thenReturn(mockKeyboard);

        // Act
        commandHandler.handleCommand(message, "/template", session, user);

        // Assert
        assertEquals(UserState.WAITING_FOR_TEMPLATE_SELECTION, session.getState());
        verify(messageService).getTemplateChooseMessage();
        verify(memeService).getAvailableTemplates();
        verify(keyboardFactory).createTemplateKeyboard(templates);
        verify(bot).execute(any(SendMessage.class));
        assertEquals("/template", session.getLastCommand());
    }

    @Test
    public void testHandleContestCommand() throws TelegramApiException {
        // Arrange
        String contestInfoMessage = "Contest information";
        when(messageService.getContestInfoMessage()).thenReturn(contestInfoMessage);

        // Act
        commandHandler.handleCommand(message, "/contest", session, user);

        // Assert
        verify(messageService).getContestInfoMessage();
        verify(bot).execute(any(SendMessage.class));
        assertEquals("/contest", session.getLastCommand());
    }

    @Test
    public void testHandleNftCommand() throws TelegramApiException {
        // Arrange
        String nftInfoMessage = "NFT information";
        when(messageService.getNftInfoMessage()).thenReturn(nftInfoMessage);

        // Act
        commandHandler.handleCommand(message, "/nft", session, user);

        // Assert
        verify(messageService).getNftInfoMessage();
        verify(bot).execute(any(SendMessage.class));
        assertEquals("/nft", session.getLastCommand());
    }

    @Test
    public void testHandleUnknownCommand() throws TelegramApiException {
        // Arrange
        String unknownCommandMessage = "Unknown command";
        when(messageService.getUnknownCommandMessage()).thenReturn(unknownCommandMessage);

        // Act
        commandHandler.handleCommand(message, "/unknown", session, user);

        // Assert
        verify(messageService).getUnknownCommandMessage();
        verify(bot).execute(any(SendMessage.class));
        assertEquals("/unknown", session.getLastCommand());
    }

    @Test
    public void testHandleTelegramApiException() throws TelegramApiException {
        // Arrange
        when(messageService.getWelcomeMessage()).thenReturn("Welcome");
        when(keyboardFactory.createMainMenuKeyboard()).thenReturn(mockKeyboard);
        doThrow(new TelegramApiException("Test exception")).when(bot).execute(any(SendMessage.class));

        // Act
        commandHandler.handleCommand(message, "/start", session, user);

        // Assert
        // Verify the method doesn't throw exception outside
        verify(bot).execute(any(SendMessage.class));
        assertEquals("/start", session.getLastCommand());
    }
} 