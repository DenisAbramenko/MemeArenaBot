package org.abr.memearenabot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private MessageService messageService;

    private final String messageKey = "test.key";
    private final String messageText = "Test message";
    private final Object[] args = new Object[]{"arg1", 2};
    private final String messageWithArgs = "Test message with args: arg1, 2";

    @BeforeEach
    public void setUp() {
        // Set default locale to English
        when(messageSource.getMessage(eq(messageKey), isNull(), any(Locale.class)))
            .thenReturn(messageText);
        
        // Set message with arguments
        when(messageSource.getMessage(eq(messageKey), eq(args), any(Locale.class)))
            .thenReturn(messageWithArgs);
    }

    @Test
    public void testGetMessage_NoArgs() {
        String result = messageService.getMessage(messageKey);
        
        assertEquals(messageText, result);
        verify(messageSource).getMessage(eq(messageKey), isNull(), any(Locale.class));
    }

    @Test
    public void testGetMessage_WithArgs() {
        String result = messageService.getMessage(messageKey, args);
        
        assertEquals(messageWithArgs, result);
        verify(messageSource).getMessage(eq(messageKey), eq(args), any(Locale.class));
    }

    @Test
    public void testGetMessage_WithLocale() {
        Locale russianLocale = new Locale("ru");
        String russianMessage = "Тестовое сообщение";
        
        when(messageSource.getMessage(eq(messageKey), isNull(), eq(russianLocale)))
            .thenReturn(russianMessage);
        
        String result = messageService.getMessage(messageKey, russianLocale);
        
        assertEquals(russianMessage, result);
        verify(messageSource).getMessage(eq(messageKey), isNull(), eq(russianLocale));
    }

    @Test
    public void testGetMessage_WithArgsAndLocale() {
        Locale russianLocale = new Locale("ru");
        String russianMessageWithArgs = "Тестовое сообщение с аргументами: arg1, 2";
        
        when(messageSource.getMessage(eq(messageKey), eq(args), eq(russianLocale)))
            .thenReturn(russianMessageWithArgs);
        
        String result = messageService.getMessage(messageKey, russianLocale, args);
        
        assertEquals(russianMessageWithArgs, result);
        verify(messageSource).getMessage(eq(messageKey), eq(args), eq(russianLocale));
    }

    @Test
    public void testGetMessage_KeyNotFound() {
        String nonExistentKey = "non.existent.key";
        String defaultMessage = "Key not found: " + nonExistentKey;
        
        when(messageSource.getMessage(eq(nonExistentKey), isNull(), any(Locale.class)))
            .thenReturn(defaultMessage);
        
        String result = messageService.getMessage(nonExistentKey);
        
        assertEquals(defaultMessage, result);
        verify(messageSource).getMessage(eq(nonExistentKey), isNull(), any(Locale.class));
    }

    @Test
    public void testGetWelcomeMessage() {
        when(messageSource.getMessage(eq("welcome.message"), isNull(), any(Locale.class)))
            .thenReturn("Welcome to Meme Arena Bot!");
        
        String result = messageService.getWelcomeMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("welcome.message"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetHelpMessage() {
        when(messageSource.getMessage(eq("help.message"), isNull(), any(Locale.class)))
            .thenReturn("Here's how to use the bot...");
        
        String result = messageService.getHelpMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("help.message"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetAiPromptMessage() {
        when(messageSource.getMessage(eq("command.ai.prompt"), isNull(), any(Locale.class)))
            .thenReturn("Describe the meme you want to generate");
        
        String result = messageService.getAiPromptMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("command.ai.prompt"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetTemplateChooseMessage() {
        when(messageSource.getMessage(eq("command.template.choose"), isNull(), any(Locale.class)))
            .thenReturn("Choose a template");
        
        String result = messageService.getTemplateChooseMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("command.template.choose"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetContestInfoMessage() {
        when(messageSource.getMessage(eq("command.contest.info"), isNull(), any(Locale.class)))
            .thenReturn("Contest information");
        
        String result = messageService.getContestInfoMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("command.contest.info"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetNftInfoMessage() {
        when(messageSource.getMessage(eq("command.nft.info"), isNull(), any(Locale.class)))
            .thenReturn("NFT information");
        
        String result = messageService.getNftInfoMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("command.nft.info"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetUnknownCommandMessage() {
        when(messageSource.getMessage(eq("command.unknown"), isNull(), any(Locale.class)))
            .thenReturn("Unknown command");
        
        String result = messageService.getUnknownCommandMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("command.unknown"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetMemeActionPublishMessage() {
        when(messageSource.getMessage(eq("meme.action.publish"), isNull(), any(Locale.class)))
            .thenReturn("Publish to feed");
        
        String result = messageService.getMemeActionPublishMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("meme.action.publish"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetMemeActionContestMessage() {
        when(messageSource.getMessage(eq("meme.action.contest"), isNull(), any(Locale.class)))
            .thenReturn("Submit to contest");
        
        String result = messageService.getMemeActionContestMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("meme.action.contest"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetMemeActionNftMessage() {
        when(messageSource.getMessage(eq("meme.action.nft"), isNull(), any(Locale.class)))
            .thenReturn("Create NFT");
        
        String result = messageService.getMemeActionNftMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("meme.action.nft"), isNull(), any(Locale.class));
    }

    @Test
    public void testGetMemeActionNewMessage() {
        when(messageSource.getMessage(eq("meme.action.new"), isNull(), any(Locale.class)))
            .thenReturn("Create new meme");
        
        String result = messageService.getMemeActionNewMessage();
        
        assertNotNull(result);
        verify(messageSource).getMessage(eq("meme.action.new"), isNull(), any(Locale.class));
    }
} 