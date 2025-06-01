package org.abr.memearenabot.bot.keyboard;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InlineKeyboardFactoryTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private MessageService messageService;

    private InlineKeyboardFactory inlineKeyboardFactory;
    private final String memeUrl = "https://example.com/meme.jpg";

    @BeforeEach
    public void setUp() {
        inlineKeyboardFactory = new InlineKeyboardFactory(bot, messageService);
    }

    @Test
    public void testCreateMemeActionsKeyboard() {
        // Arrange
        String publishButton = "📢 Publish to Feed";
        String contestButton = "🏆 Submit to Contest";
        String nftButton = "💎 Create NFT";
        String newButton = "🆕 Create New Meme";

        when(messageService.getMemeActionPublishMessage()).thenReturn(publishButton);
        when(messageService.getMemeActionContestMessage()).thenReturn(contestButton);
        when(messageService.getMemeActionNftMessage()).thenReturn(nftButton);
        when(messageService.getMemeActionNewMessage()).thenReturn(newButton);

        // Act
        InlineKeyboardMarkup keyboard = inlineKeyboardFactory.createMemeActionsKeyboard(memeUrl);

        // Assert
        assertNotNull(keyboard);
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(2, rows.size());

        // First row
        assertEquals(2, rows.get(0).size());
        assertEquals(publishButton, rows.get(0).get(0).getText());
        assertEquals("publish:" + memeUrl, rows.get(0).get(0).getCallbackData());
        assertEquals(contestButton, rows.get(0).get(1).getText());
        assertEquals("contest:" + memeUrl, rows.get(0).get(1).getCallbackData());

        // Second row
        assertEquals(2, rows.get(1).size());
        assertEquals(nftButton, rows.get(1).get(0).getText());
        assertEquals("nft:" + memeUrl, rows.get(1).get(0).getCallbackData());
        assertEquals(newButton, rows.get(1).get(1).getText());
        assertEquals("new", rows.get(1).get(1).getCallbackData());
    }

    @Test
    public void testCreateVoteKeyboard() {
        // Arrange
        String voteButton = "👍 Like";
        Long memeId = 123L;

        when(messageService.getMessage("button.vote")).thenReturn(voteButton);

        // Act
        InlineKeyboardMarkup keyboard = inlineKeyboardFactory.createVoteKeyboard(memeId);

        // Assert
        assertNotNull(keyboard);
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(1, rows.size());
        assertEquals(1, rows.get(0).size());
        assertEquals("👍 Like", rows.get(0).get(0).getText());
        assertEquals("vote:" + memeId, rows.get(0).get(0).getCallbackData());
    }

    @Test
    public void testCreateTemplateSelectionKeyboard() {
        // Arrange
        List<String> templates = Arrays.asList("drake", "distracted", "button");

        // Act
        InlineKeyboardMarkup keyboard = inlineKeyboardFactory.createTemplateSelectionKeyboard(templates);

        // Assert
        assertNotNull(keyboard);
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(templates.size() + 1, rows.size()); // Templates + back button

        // Template rows
        for (int i = 0; i < templates.size(); i++) {
            assertEquals(1, rows.get(i).size());
            assertEquals(templates.get(i), rows.get(i).get(0).getText());
            assertEquals("template:" + templates.get(i), rows.get(i).get(0).getCallbackData());
        }

        // Back button row
        assertEquals(1, rows.get(templates.size()).size());
        assertEquals("↩ Back", rows.get(templates.size()).get(0).getText());
        assertEquals("back", rows.get(templates.size()).get(0).getCallbackData());
    }

    @Test
    public void testCreatePaginationKeyboard() {
        // Arrange
        int currentPage = 2;
        int totalPages = 5;
        String baseCommand = "page";

        // Act
        InlineKeyboardMarkup keyboard = inlineKeyboardFactory.createPaginationKeyboard(currentPage, totalPages, baseCommand);

        // Assert
        assertNotNull(keyboard);
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(1, rows.size());
        assertEquals(3, rows.get(0).size());

        // Previous button
        assertEquals("◀️ Previous", rows.get(0).get(0).getText());
        assertEquals(baseCommand + ":" + (currentPage - 1), rows.get(0).get(0).getCallbackData());
        
        // Current page indicator
        assertEquals(currentPage + " / " + totalPages, rows.get(0).get(1).getText());
        assertEquals("noop", rows.get(0).get(1).getCallbackData());
        
        // Next button
        assertEquals("Next ▶️", rows.get(0).get(2).getText());
        assertEquals(baseCommand + ":" + (currentPage + 1), rows.get(0).get(2).getCallbackData());
    }

    @Test
    public void testCreatePaginationKeyboard_FirstPage() {
        // Arrange
        int currentPage = 1;
        int totalPages = 3;
        String baseCommand = "page";

        // Act
        InlineKeyboardMarkup keyboard = inlineKeyboardFactory.createPaginationKeyboard(currentPage, totalPages, baseCommand);

        // Assert
        assertNotNull(keyboard);
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(1, rows.size());
        
        // No previous button on first page
        assertEquals(2, rows.get(0).size());
        
        // Current page indicator
        assertEquals(currentPage + " / " + totalPages, rows.get(0).get(0).getText());
        assertEquals("noop", rows.get(0).get(0).getCallbackData());
        
        // Next button
        assertEquals("Next ▶️", rows.get(0).get(1).getText());
        assertEquals(baseCommand + ":" + (currentPage + 1), rows.get(0).get(1).getCallbackData());
    }

    @Test
    public void testCreatePaginationKeyboard_LastPage() {
        // Arrange
        int currentPage = 3;
        int totalPages = 3;
        String baseCommand = "page";

        // Act
        InlineKeyboardMarkup keyboard = inlineKeyboardFactory.createPaginationKeyboard(currentPage, totalPages, baseCommand);

        // Assert
        assertNotNull(keyboard);
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(1, rows.size());
        
        // No next button on last page
        assertEquals(2, rows.get(0).size());
        
        // Previous button
        assertEquals("◀️ Previous", rows.get(0).get(0).getText());
        assertEquals(baseCommand + ":" + (currentPage - 1), rows.get(0).get(0).getCallbackData());
        
        // Current page indicator
        assertEquals(currentPage + " / " + totalPages, rows.get(0).get(1).getText());
        assertEquals("noop", rows.get(0).get(1).getCallbackData());
    }
} 