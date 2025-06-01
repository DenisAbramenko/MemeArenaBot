package org.abr.memearenabot.bot.keyboard;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeyboardFactoryTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private MessageService messageService;

    private KeyboardFactory keyboardFactory;

    @BeforeEach
    public void setUp() {
        keyboardFactory = new KeyboardFactory(bot, messageService);
    }

    @Test
    public void testCreateMainMenuKeyboard() {
        // Arrange
        String aiCommand = "🧠 AI Meme";
        String templateCommand = "📝 Template Meme";
        String voiceCommand = "🎙 Voice Meme";
        String contestCommand = "🏆 Contest";
        String monetizationCommand = "💰 Monetization";
        String helpCommand = "❓ Help";

        when(messageService.getKeyboardAiMessage()).thenReturn(aiCommand);
        when(messageService.getKeyboardTemplateMessage()).thenReturn(templateCommand);
        when(messageService.getKeyboardVoiceMessage()).thenReturn(voiceCommand);
        when(messageService.getKeyboardContestMessage()).thenReturn(contestCommand);
        when(messageService.getKeyboardMonetizationMessage()).thenReturn(monetizationCommand);
        when(messageService.getKeyboardHelpMessage()).thenReturn(helpCommand);

        // Act
        ReplyKeyboardMarkup keyboard = keyboardFactory.createMainMenuKeyboard();

        // Assert
        assertNotNull(keyboard);
        assertTrue(keyboard.getResizeKeyboard());
        assertFalse(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(3, rows.size());

        // First row
        assertEquals(2, rows.get(0).size());
        assertEquals(aiCommand, rows.get(0).get(0).getText());
        assertEquals(templateCommand, rows.get(0).get(1).getText());

        // Second row
        assertEquals(2, rows.get(1).size());
        assertEquals(voiceCommand, rows.get(1).get(0).getText());
        assertEquals(contestCommand, rows.get(1).get(1).getText());

        // Third row
        assertEquals(2, rows.get(2).size());
        assertEquals(monetizationCommand, rows.get(2).get(0).getText());
        assertEquals(helpCommand, rows.get(2).get(1).getText());
    }

    @Test
    public void testCreateTemplateKeyboard() {
        // Arrange
        List<String> templates = Arrays.asList("drake", "distracted", "button", "expanding");
        String backButton = "↩ Back";

        // Act
        ReplyKeyboardMarkup keyboard = keyboardFactory.createTemplateKeyboard(templates);

        // Assert
        assertNotNull(keyboard);
        assertTrue(keyboard.getResizeKeyboard());
        assertTrue(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        
        // Templates should be arranged with 2 per row, plus back button row
        int expectedRows = (templates.size() + 1) / 2 + 1;
        assertEquals(expectedRows, rows.size());

        // Check first row with templates
        assertEquals(2, rows.get(0).size());
        assertEquals(templates.get(0), rows.get(0).get(0).getText());
        assertEquals(templates.get(1), rows.get(0).get(1).getText());
        
        // Check second row with templates
        assertEquals(2, rows.get(1).size());
        assertEquals(templates.get(2), rows.get(1).get(0).getText());
        assertEquals(templates.get(3), rows.get(1).get(1).getText());

        // Check back button row
        assertEquals(1, rows.get(expectedRows - 1).size());
        assertEquals("↩ Back", rows.get(expectedRows - 1).get(0).getText());
    }

    @Test
    public void testCreateTemplateKeyboard_OddNumberOfTemplates() {
        // Arrange
        List<String> templates = Arrays.asList("drake", "distracted", "button");

        // Act
        ReplyKeyboardMarkup keyboard = keyboardFactory.createTemplateKeyboard(templates);

        // Assert
        assertNotNull(keyboard);
        
        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        
        // 2 templates per row (one row with 2, one with 1), plus back button row
        assertEquals(3, rows.size());

        // Check first row with 2 templates
        assertEquals(2, rows.get(0).size());
        assertEquals(templates.get(0), rows.get(0).get(0).getText());
        assertEquals(templates.get(1), rows.get(0).get(1).getText());
        
        // Check second row with 1 template
        assertEquals(1, rows.get(1).size());
        assertEquals(templates.get(2), rows.get(1).get(0).getText());

        // Check back button row
        assertEquals(1, rows.get(2).size());
        assertEquals("↩ Back", rows.get(2).get(0).getText());
    }

    @Test
    public void testCreateVoiceRequestKeyboard() {
        // Act
        ReplyKeyboardMarkup keyboard = keyboardFactory.createVoiceRequestKeyboard();

        // Assert
        assertNotNull(keyboard);
        assertTrue(keyboard.getResizeKeyboard());
        assertFalse(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(2, rows.size());

        // First row with voice button
        assertEquals(1, rows.get(0).size());
        KeyboardButton voiceButton = rows.get(0).get(0);
        assertEquals("🎙 Record Voice", voiceButton.getText());
        assertFalse(voiceButton.getRequestContact());
        assertFalse(voiceButton.getRequestLocation());
        assertNull(voiceButton.getRequestPoll());

        // Second row with back button
        assertEquals(1, rows.get(1).size());
        assertEquals("↩ Back", rows.get(1).get(0).getText());
    }

    @Test
    public void testCreateMemeActionKeyboard() {
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
        ReplyKeyboardMarkup keyboard = keyboardFactory.createMemeActionKeyboard();

        // Assert
        assertNotNull(keyboard);
        assertTrue(keyboard.getResizeKeyboard());
        assertFalse(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(2, rows.size());

        // First row
        assertEquals(2, rows.get(0).size());
        assertEquals(publishButton, rows.get(0).get(0).getText());
        assertEquals(contestButton, rows.get(0).get(1).getText());

        // Second row
        assertEquals(2, rows.get(1).size());
        assertEquals(nftButton, rows.get(1).get(0).getText());
        assertEquals(newButton, rows.get(1).get(1).getText());
    }

    @Test
    public void testCreateAdminMenuKeyboard() {
        // Arrange
        String usersButton = "👥 Пользователи";
        String statsButton = "📊 Статистика";
        String settingsButton = "⚙️ Настройки";
        String broadcastButton = "📣 Рассылка";
        String maintenanceButton = "🛠️ Обслуживание";
        String backButton = "↩️ Назад";

        when(messageService.getMessage("admin.button.users")).thenReturn(usersButton);
        when(messageService.getMessage("admin.button.stats")).thenReturn(statsButton);
        when(messageService.getMessage("admin.button.settings")).thenReturn(settingsButton);
        when(messageService.getMessage("admin.button.broadcast")).thenReturn(broadcastButton);
        when(messageService.getMessage("admin.button.maintenance")).thenReturn(maintenanceButton);
        when(messageService.getMessage("admin.button.back")).thenReturn(backButton);

        // Act
        ReplyKeyboardMarkup keyboard = keyboardFactory.createAdminMenuKeyboard();

        // Assert
        assertNotNull(keyboard);
        assertTrue(keyboard.getResizeKeyboard());
        assertFalse(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(3, rows.size());

        // First row
        assertEquals(2, rows.get(0).size());
        assertEquals(usersButton, rows.get(0).get(0).getText());
        assertEquals(statsButton, rows.get(0).get(1).getText());

        // Second row
        assertEquals(2, rows.get(1).size());
        assertEquals(settingsButton, rows.get(1).get(0).getText());
        assertEquals(broadcastButton, rows.get(1).get(1).getText());

        // Third row
        assertEquals(2, rows.get(2).size());
        assertEquals(maintenanceButton, rows.get(2).get(0).getText());
        assertEquals(backButton, rows.get(2).get(1).getText());
    }

    @Test
    public void testCreateUserManagementKeyboard() {
        // Arrange
        String listButton = "📋 Список пользователей";
        String searchButton = "🔍 Найти пользователя";
        String premiumButton = "💎 Премиум пользователи";
        String blockButton = "🚫 Заблокированные";
        String inactiveButton = "💤 Неактивные";
        String backButton = "↩️ Назад";

        when(messageService.getMessage("admin.users.list")).thenReturn(listButton);
        when(messageService.getMessage("admin.users.search")).thenReturn(searchButton);
        when(messageService.getMessage("admin.users.premium")).thenReturn(premiumButton);
        when(messageService.getMessage("admin.users.block")).thenReturn(blockButton);
        when(messageService.getMessage("admin.users.inactive")).thenReturn(inactiveButton);
        when(messageService.getMessage("admin.button.back")).thenReturn(backButton);

        // Act
        ReplyKeyboardMarkup keyboard = keyboardFactory.createUserManagementKeyboard();

        // Assert
        assertNotNull(keyboard);
        assertTrue(keyboard.getResizeKeyboard());
        assertFalse(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(3, rows.size());

        // First row
        assertEquals(2, rows.get(0).size());
        assertEquals(listButton, rows.get(0).get(0).getText());
        assertEquals(searchButton, rows.get(0).get(1).getText());

        // Second row
        assertEquals(2, rows.get(1).size());
        assertEquals(premiumButton, rows.get(1).get(0).getText());
        assertEquals(blockButton, rows.get(1).get(1).getText());

        // Third row
        assertEquals(2, rows.get(2).size());
        assertEquals(inactiveButton, rows.get(2).get(0).getText());
        assertEquals(backButton, rows.get(2).get(1).getText());
    }

    @Test
    public void testCreateSettingsKeyboard() {
        // Arrange
        String aiButton = "🧠 AI генерация";
        String voiceButton = "🎙️ Голосовые мемы";
        String templatesButton = "📝 Шаблоны";
        String contestButton = "🏆 Конкурсы";
        String backButton = "↩️ Назад";

        when(messageService.getMessage("admin.settings.ai")).thenReturn(aiButton);
        when(messageService.getMessage("admin.settings.voice")).thenReturn(voiceButton);
        when(messageService.getMessage("admin.settings.templates")).thenReturn(templatesButton);
        when(messageService.getMessage("admin.settings.contest")).thenReturn(contestButton);
        when(messageService.getMessage("admin.button.back")).thenReturn(backButton);

        // Act
        ReplyKeyboardMarkup keyboard = keyboardFactory.createSettingsKeyboard();

        // Assert
        assertNotNull(keyboard);
        assertTrue(keyboard.getResizeKeyboard());
        assertFalse(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertNotNull(rows);
        assertEquals(3, rows.size());

        // First row
        assertEquals(2, rows.get(0).size());
        assertEquals(aiButton, rows.get(0).get(0).getText());
        assertEquals(voiceButton, rows.get(0).get(1).getText());

        // Second row
        assertEquals(2, rows.get(1).size());
        assertEquals(templatesButton, rows.get(1).get(0).getText());
        assertEquals(contestButton, rows.get(1).get(1).getText());

        // Third row
        assertEquals(1, rows.get(2).size());
        assertEquals(backButton, rows.get(2).get(0).getText());
    }
} 