package org.abr.memearenabot.bot.handler;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.bot.sender.MessageSender;
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
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CallbackHandlerTest {

    @Mock
    private MemeService memeService;

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

    @Mock
    private MessageSender messageSender;

    @Mock
    private TelegramBot bot;

    @Mock
    private CallbackQuery callbackQuery;

    @Mock
    private Message message;

    private CallbackHandler callbackHandler;
    private UserSession session;
    private User user;
    private final Long chatId = 123456789L;
    private final String memeUrl = "https://example.com/meme.jpg";

    @BeforeEach
    public void setUp() {
        callbackHandler = new CallbackHandler(memeService, userService, messageService, messageSender, bot);
        session = new UserSession();
        user = new User(chatId.toString(), "test_user", "Test", "User", "en");
        user.setId(1L);

        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(1000);
    }

    @Test
    public void testHandlePublishCallback() throws TelegramApiException {
        // Arrange
        String callbackData = "publish:" + memeUrl;
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        when(memeService.publishMemeToFeed(memeUrl, user.getTelegramId())).thenReturn(true);

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(memeService).publishMemeToFeed(memeUrl, user.getTelegramId());
        verify(messageSender).sendLocalizedText(chatId, "meme.publish.success");
    }

    @Test
    public void testHandlePublishCallback_Failure() throws TelegramApiException {
        // Arrange
        String callbackData = "publish:" + memeUrl;
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        when(memeService.publishMemeToFeed(memeUrl, user.getTelegramId())).thenReturn(false);

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(memeService).publishMemeToFeed(memeUrl, user.getTelegramId());
        verify(messageSender).sendLocalizedText(chatId, "meme.publish.error");
    }

    @Test
    public void testHandleContestCallback() throws TelegramApiException {
        // Arrange
        String callbackData = "contest:" + memeUrl;
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        when(memeService.submitMemeToContest(memeUrl, user.getTelegramId())).thenReturn(true);

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(memeService).submitMemeToContest(memeUrl, user.getTelegramId());
        verify(messageSender).sendLocalizedText(chatId, "meme.contest.success");
    }

    @Test
    public void testHandleContestCallback_Failure() throws TelegramApiException {
        // Arrange
        String callbackData = "contest:" + memeUrl;
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        when(memeService.submitMemeToContest(memeUrl, user.getTelegramId())).thenReturn(false);

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(memeService).submitMemeToContest(memeUrl, user.getTelegramId());
        verify(messageSender).sendLocalizedText(chatId, "meme.contest.error");
    }

    @Test
    public void testHandleNftCallback() throws TelegramApiException {
        // Arrange
        String callbackData = "nft:" + memeUrl;
        String nftUrl = "https://example.com/nft/123";
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(messageSender).sendLocalizedText(chatId, "meme.nft.success", nftUrl);
    }

    @Test
    public void testHandleNftCallback_Failure() throws TelegramApiException {
        // Arrange
        String callbackData = "nft:" + memeUrl;
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(messageSender).sendLocalizedText(chatId, "meme.nft.error");
    }

    @Test
    public void testHandleVoteCallback() throws TelegramApiException {
        // Arrange
        String callbackData = "vote:123";
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        when(memeService.voteMeme(123L)).thenReturn(true);

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(memeService).voteMeme(123L);
        verify(messageSender).sendText(chatId, "👍 Спасибо за ваш голос!");
    }

    @Test
    public void testHandleVoteCallback_Failure() throws TelegramApiException {
        // Arrange
        String callbackData = "vote:123";
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        when(memeService.voteMeme(123L)).thenReturn(false);

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(memeService).voteMeme(123L);
        verify(messageSender).sendText(chatId, "Не удалось проголосовать. Возможно, вы уже голосовали за этот мем.");
    }

    @Test
    public void testHandleBackCallback() throws TelegramApiException {
        // Arrange
        String callbackData = "back";
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        assertEquals(UserState.IDLE, session.getState());
        verify(messageSender).sendLocalizedText(chatId, "welcome.action");
    }

    @Test
    public void testHandleNewCallback() throws TelegramApiException {
        // Arrange
        String callbackData = "new";
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        assertEquals(UserState.IDLE, session.getState());
        verify(messageSender).sendLocalizedText(chatId, "welcome.action");
    }

    @Test
    public void testHandleUnknownCallback() throws TelegramApiException {
        // Arrange
        String callbackData = "unknown:data";
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(messageSender).sendLocalizedText(chatId, "common.error");
    }

    @Test
    public void testHandleCallbackWithException() throws TelegramApiException {
        // Arrange
        String callbackData = "publish:" + memeUrl;
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn("callback123");
        doThrow(new TelegramApiException("Test exception")).when(bot).execute(any(AnswerCallbackQuery.class));

        // Act
        callbackHandler.handleCallback(callbackQuery, session, user);

        // Assert
        verify(bot).execute(any(AnswerCallbackQuery.class));
        verify(messageSender).sendLocalizedText(chatId, "common.error");
    }
} 