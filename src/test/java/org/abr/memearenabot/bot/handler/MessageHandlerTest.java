package org.abr.memearenabot.bot.handler;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.bot.keyboard.KeyboardFactory;
import org.abr.memearenabot.bot.sender.MessageSender;
import org.abr.memearenabot.bot.session.UserSession;
import org.abr.memearenabot.bot.session.UserState;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.service.ContestService;
import org.abr.memearenabot.service.MemeService;
import org.abr.memearenabot.service.MessageService;
import org.abr.memearenabot.service.UserService;
import org.abr.memearenabot.validation.InputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTest {

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
    private MessageSender messageSender;

    @Mock
    ContestService contestService;

    @Mock
    private InputValidator inputValidator;

    @Mock
    private Message message;

    @Mock
    private Voice voice;

    @Mock
    private ReplyKeyboardMarkup actionsKeyboard;

    private MessageHandler messageHandler;
    private UserSession session;
    private User user;
    private final Long chatId = 123456789L;
    private final String text = "Generate a funny cat meme";

    @BeforeEach
    public void setUp() {
        messageHandler = new MessageHandler(bot, memeService, userService, messageService, keyboardFactory, messageSender, inputValidator, contestService);
        session = new UserSession();
        user = new User(chatId.toString(), "test_user", "Test", "User", "en");
        user.setId(1L);

        when(message.getChatId()).thenReturn(chatId);
    }

    @Test
    public void testHandleTextMessage_IdleState() {
        // Arrange
        session.setState(UserState.IDLE);
        when(inputValidator.validateAiDescription(text)).thenReturn(InputValidator.ValidationResult.success());
        when(messageSender.sendLocalizedText(eq(chatId), eq("meme.generating.ai"))).thenReturn(message);
        when(memeService.generateMeme(text, user)).thenReturn(CompletableFuture.completedFuture("https://example.com/meme.jpg"));
        when(keyboardFactory.createMemeActionKeyboard()).thenReturn(actionsKeyboard);

        // Act
        messageHandler.handleTextMessage(message, text, session, user);

        // Assert
        verify(inputValidator).validateAiDescription(text);
        verify(messageSender).sendLocalizedText(eq(chatId), eq("meme.generating.ai"));
        verify(memeService).generateMeme(text, user);
    }

    @Test
    public void testHandleTextMessage_WaitingForAiDescription() {
        // Arrange
        session.setState(UserState.WAITING_FOR_AI_DESCRIPTION);
        when(inputValidator.validateAiDescription(text)).thenReturn(InputValidator.ValidationResult.success());
        when(messageSender.sendLocalizedText(eq(chatId), eq("meme.generating.ai"))).thenReturn(message);
        when(memeService.generateMeme(text, user)).thenReturn(CompletableFuture.completedFuture("https://example.com/meme.jpg"));
        when(keyboardFactory.createMemeActionKeyboard()).thenReturn(actionsKeyboard);

        // Act
        messageHandler.handleTextMessage(message, text, session, user);

        // Assert
        verify(inputValidator).validateAiDescription(text);
        verify(messageSender).sendLocalizedText(eq(chatId), eq("meme.generating.ai"));
        verify(memeService).generateMeme(text, user);
    }

    @Test
    public void testHandleTextMessage_WaitingForTemplateSelection() {
        // Arrange
        session.setState(UserState.WAITING_FOR_TEMPLATE_SELECTION);
        String templateId = "drake";
        when(memeService.getAvailableTemplates()).thenReturn(Arrays.asList(templateId));

        // Act
        messageHandler.handleTextMessage(message, templateId, session, user);

        // Assert
        assertEquals(UserState.WAITING_FOR_TEMPLATE_TEXT, session.getState());
        assertEquals(templateId, session.getSelectedTemplate());
        verify(messageSender).sendLocalizedText(eq(chatId), eq("command.template.text"), eq(templateId));
    }

    @Test
    public void testHandleTextMessage_WaitingForTemplateText() {
        // Arrange
        session.setState(UserState.WAITING_FOR_TEMPLATE_TEXT);
        session.setSelectedTemplate("drake");
        String text = "Line 1\nLine 2";
        when(inputValidator.validateTemplateTextLines(anyList())).thenReturn(InputValidator.ValidationResult.success());
        when(messageSender.sendLocalizedText(eq(chatId), eq("meme.generating.template"))).thenReturn(message);
        when(memeService.generateMemeFromTemplate(eq("drake"), anyList(), eq(user)))
                .thenReturn(CompletableFuture.completedFuture("https://example.com/meme.jpg"));
        when(keyboardFactory.createMemeActionKeyboard()).thenReturn(actionsKeyboard);

        // Act
        messageHandler.handleTextMessage(message, text, session, user);

        // Assert
        verify(inputValidator).validateTemplateTextLines(anyList());
        verify(messageSender).sendLocalizedText(eq(chatId), eq("meme.generating.template"));
        verify(memeService).generateMemeFromTemplate(eq("drake"), anyList(), eq(user));
    }

    @Test
    public void testHandleTextMessage_MemeGenerated_Publish() {
        // Arrange
        session.setState(UserState.MEME_GENERATED);
        session.setLastMemeUrl("https://example.com/meme.jpg");
        when(messageService.getMemeActionPublishMessage()).thenReturn("Publish to feed");
        when(memeService.publishMemeToFeed(eq("https://example.com/meme.jpg"), eq(user.getTelegramId()))).thenReturn(true);

        // Act
        messageHandler.handleTextMessage(message, "Publish to feed", session, user);

        // Assert
        assertEquals(UserState.IDLE, session.getState());
        verify(memeService).publishMemeToFeed(eq("https://example.com/meme.jpg"), eq(user.getTelegramId()));
        verify(messageSender).sendLocalizedText(eq(chatId), eq("meme.publish.success"));
    }

    @Test
    public void testHandleTextMessage_MemeGenerated_Contest() {
        // Arrange
        session.setState(UserState.MEME_GENERATED);
        session.setLastMemeUrl("https://example.com/meme.jpg");
        when(messageService.getMemeActionContestMessage()).thenReturn("Submit to contest");
        when(memeService.submitMemeToContest(eq("https://example.com/meme.jpg"), eq(user.getTelegramId()))).thenReturn(true);

        // Act
        messageHandler.handleTextMessage(message, "Submit to contest", session, user);

        // Assert
        assertEquals(UserState.IDLE, session.getState());
        verify(memeService).submitMemeToContest(eq("https://example.com/meme.jpg"), eq(user.getTelegramId()));
        verify(messageSender).sendLocalizedText(eq(chatId), eq("meme.contest.success"));
    }

    @Test
    public void testHandleTextMessage_MemeGenerated_NFT() {
        // Arrange
        session.setState(UserState.MEME_GENERATED);
        session.setLastMemeUrl("https://example.com/meme.jpg");
        when(messageService.getMemeActionNftMessage()).thenReturn("Create NFT");
        when(memeService.createNFT(eq("https://example.com/meme.jpg"), eq(user.getTelegramId())))
                .thenReturn("https://example.com/nft/123");

        // Act
        messageHandler.handleTextMessage(message, "Create NFT", session, user);

        // Assert
        assertEquals(UserState.IDLE, session.getState());
        verify(memeService).createNFT(eq("https://example.com/meme.jpg"), eq(user.getTelegramId()));
        verify(messageSender).sendLocalizedText(eq(chatId), eq("meme.nft.success"), eq("https://example.com/nft/123"));
    }

    @Test
    public void testHandleTextMessage_MemeGenerated_New() {
        // Arrange
        session.setState(UserState.MEME_GENERATED);
        session.setLastMemeUrl("https://example.com/meme.jpg");
        when(messageService.getMemeActionNewMessage()).thenReturn("Create new meme");
        when(keyboardFactory.createMainMenuKeyboard()).thenReturn(actionsKeyboard);

        // Act
        messageHandler.handleTextMessage(message, "Create new meme", session, user);

        // Assert
        assertEquals(UserState.IDLE, session.getState());
        verify(keyboardFactory).createMainMenuKeyboard();
        verify(messageSender).sendLocalizedText(eq(chatId), eq("welcome.action"), eq(actionsKeyboard));
    }

    @Test
    public void testHandleVoiceMessage() throws TelegramApiException, IOException {
        // Arrange
        when(message.getVoice()).thenReturn(voice);
        when(messageSender.sendLocalizedText(eq(chatId), eq("meme.generating.voice"))).thenReturn(message);

        // Mock GetFile execution
        File voiceFile = mock(File.class);
        when(voice.getFileId()).thenReturn("voice123");
        when(bot.execute(any(GetFile.class))).thenReturn(voiceFile);
        when(voiceFile.getFileUrl(anyString())).thenReturn("https://example.com/voice.ogg");
        
        // Mock voice data validation
        byte[] voiceData = "test voice data".getBytes();
        when(inputValidator.validateVoiceData(any(byte[].class))).thenReturn(InputValidator.ValidationResult.success());
        
        // Mock meme generation
        when(memeService.generateMemeFromVoice(any(byte[].class), eq(user)))
                .thenReturn(CompletableFuture.completedFuture("https://example.com/meme.jpg"));
        when(keyboardFactory.createMemeActionKeyboard()).thenReturn(actionsKeyboard);

        // Act
        messageHandler.handleVoiceMessage(message, session, user);

        // Assert
        verify(messageSender).sendLocalizedText(eq(chatId), eq("meme.generating.voice"));
        verify(bot).execute(any(GetFile.class));
    }

    @Test
    public void testHandleInvalidAiDescription() {
        // Arrange
        session.setState(UserState.WAITING_FOR_AI_DESCRIPTION);
        when(inputValidator.validateAiDescription(text)).thenReturn(
                InputValidator.ValidationResult.error("Description is too long"));

        // Act
        messageHandler.handleTextMessage(message, text, session, user);

        // Assert
        verify(inputValidator).validateAiDescription(text);
        verify(messageSender).sendText(eq(chatId), eq("Description is too long"));
        verify(memeService, never()).generateMeme(anyString(), any(User.class));
    }

    @Test
    public void testHandleTemplateSelectionBack() {
        // Arrange
        session.setState(UserState.WAITING_FOR_TEMPLATE_SELECTION);
        when(keyboardFactory.createMainMenuKeyboard()).thenReturn(actionsKeyboard);

        // Act
        messageHandler.handleTextMessage(message, "↩ Back", session, user);

        // Assert
        assertEquals(UserState.IDLE, session.getState());
        verify(keyboardFactory).createMainMenuKeyboard();
        verify(messageSender).sendLocalizedText(eq(chatId), eq("welcome.action"), eq(actionsKeyboard));
    }

    @Test
    public void testHandleTemplateSelectionInvalidTemplate() {
        // Arrange
        session.setState(UserState.WAITING_FOR_TEMPLATE_SELECTION);
        when(memeService.getAvailableTemplates()).thenReturn(Arrays.asList("drake", "button"));

        // Act
        messageHandler.handleTextMessage(message, "invalid_template", session, user);

        // Assert
        verify(memeService).getAvailableTemplates();
        verify(messageSender).sendLocalizedText(eq(chatId), eq("command.template.choose"));
    }
} 