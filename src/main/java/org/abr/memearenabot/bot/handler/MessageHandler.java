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
import org.abr.memearenabot.validation.InputValidator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

/**
 * Handler for user messages
 */
public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    
    private final TelegramBot bot;
    private final MemeService memeService;
    private final UserService userService;
    private final MessageService messageService;
    private final KeyboardFactory keyboardFactory;
    private final MessageSender messageSender;
    private final InputValidator inputValidator;
    private final ContestService contestService;
    
    public MessageHandler(
            TelegramBot bot, 
            MemeService memeService, 
            UserService userService,
            MessageService messageService,
            KeyboardFactory keyboardFactory,
            MessageSender messageSender,
            InputValidator inputValidator,
            ContestService contestService) {
        this.bot = bot;
        this.memeService = memeService;
        this.userService = userService;
        this.messageService = messageService;
        this.keyboardFactory = keyboardFactory;
        this.messageSender = messageSender;
        this.inputValidator = inputValidator;
        this.contestService = contestService;
    }
    
    /**
     * Handle text messages
     */
    public void handleTextMessage(Message message, String text, UserSession session, User user) {
        UserState state = session.getState();
        
        // Добавить обработку админ-состояний
        if (state == UserState.ADMIN_MENU) {
            handleAdminMenuMessage(message, text, session, user);
            return;
        } else if (state == UserState.ADMIN_USERS_MENU) {
            handleAdminUsersMenuMessage(message, text, session, user);
            return;
        } else if (state == UserState.ADMIN_SETTINGS_MENU) {
            handleAdminSettingsMenuMessage(message, text, session, user);
            return;
        } else if (state == UserState.ADMIN_BROADCAST_COMPOSE) {
            handleAdminBroadcastComposeMessage(message, text, session, user);
            return;
        } else if (state == UserState.ADMIN_USER_SEARCH) {
            handleAdminUserSearchMessage(message, text, session, user);
            return;
        } else if (state == UserState.ADMIN_USER_DETAIL) {
            handleAdminUserDetailMessage(message, text, session, user);
            return;
        } else if (state == UserState.ADMIN_TEMPLATE_MANAGEMENT) {
            handleAdminTemplateManagementMessage(message, text, session, user);
            return;
        }
        
        // Handle text based on current state
        switch (state) {
            case WAITING_FOR_AI_DESCRIPTION:
                handleAiDescription(message.getChatId(), text, session, user);
                break;
            case WAITING_FOR_TEMPLATE_SELECTION:
                handleTemplateSelection(message.getChatId(), text, session, user);
                break;
            case WAITING_FOR_TEMPLATE_TEXT:
                handleTemplateText(message.getChatId(), text, session, user);
                break;
            case MEME_GENERATED:
                handleMemeAction(message.getChatId(), text, session, user);
                break;
            case IDLE:
            default:
                // In IDLE state, treat text as AI description
                handleAiDescription(message.getChatId(), text, session, user);
                break;
        }
    }
    
    /**
     * Handle voice messages
     */
    public void handleVoiceMessage(Message message, UserSession session, User user) {
        Long chatId = message.getChatId();
        Voice voice = message.getVoice();
        
        try {
            // Send processing message
            messageSender.sendLocalizedText(chatId, "meme.generating.voice");
            
            // Download voice file
            GetFile getFile = new GetFile();
            getFile.setFileId(voice.getFileId());
            File voiceFile = bot.execute(getFile);
            String voiceFileUrl = voiceFile.getFileUrl(bot.getBotToken());
            
            // Download voice data
            byte[] voiceData;
            try (InputStream is = new URL(voiceFileUrl).openStream()) {
                voiceData = is.readAllBytes();
            } catch (IOException e) {
                logger.error("Error downloading voice file", e);
                messageSender.sendLocalizedText(chatId, "meme.error.voice.download");
                return;
            }
            
            // Validate voice data
            ValidationResult validationResult = inputValidator.validateVoiceData(voiceData);
            if (!validationResult.isValid()) {
                messageSender.sendText(chatId, validationResult.getErrorMessage());
                return;
            }
            
            // Generate meme from voice
            CompletableFuture<String> memeUrlFuture = memeService.generateMemeFromVoice(voiceData, user);
            
            memeUrlFuture.thenAccept(memeUrl -> {
                // Set session state and meme URL
                session.setState(UserState.MEME_GENERATED);
                session.setLastMemeUrl(memeUrl);
                
                // Send meme with actions keyboard
                ReplyKeyboardMarkup actionsKeyboard = keyboardFactory.createMemeActionKeyboard();
                messageSender.sendPhotoWithLocalizedCaption(
                        chatId, 
                        memeUrl, 
                        "meme.result.voice", 
                        actionsKeyboard);
                
                logger.info("Generated voice meme for chat ID: {}", chatId);
            }).exceptionally(e -> {
                logger.error("Error generating voice meme", e);
                messageSender.sendLocalizedText(chatId, "meme.error.voice");
                return null;
            });
        } catch (TelegramApiException e) {
            logger.error("Error handling voice message", e);
            messageSender.sendLocalizedText(chatId, "meme.error.voice");
        }
    }
    
    /**
     * Handle AI description
     */
    private void handleAiDescription(Long chatId, String text, UserSession session, User user) {
        // Проверка лимита для обычных пользователей
        if (memeService.hasReachedAiLimit(user)) {
            messageSender.sendLocalizedText(chatId, "meme.error.ai.limit");
            return;
        }
        
        // Validate input
        ValidationResult validationResult = inputValidator.validateAiDescription(text);
        if (!validationResult.isValid()) {
            messageSender.sendText(chatId, validationResult.getErrorMessage());
            return;
        }
        
        // Send processing message
        messageSender.sendLocalizedText(chatId, "meme.generating.ai");
        
        try {
            // Generate meme
            CompletableFuture<String> memeUrlFuture = memeService.generateMeme(text, user);
            
            memeUrlFuture.thenAccept(memeUrl -> {
                // Set session state and meme URL
                session.setState(UserState.MEME_GENERATED);
                session.setLastMemeUrl(memeUrl);
                
                // Send meme with actions keyboard
                ReplyKeyboardMarkup actionsKeyboard = keyboardFactory.createMemeActionKeyboard();
                messageSender.sendPhotoWithLocalizedCaption(
                        chatId, 
                        memeUrl, 
                        "meme.result.ai", 
                        actionsKeyboard);
                
                logger.info("Generated AI meme for chat ID: {}", chatId);
            }).exceptionally(e -> {
                logger.error("Error generating AI meme", e);
                messageSender.sendLocalizedText(chatId, "meme.error.ai");
                return null;
            });
        } catch (Exception e) {
            logger.error("Error generating AI meme", e);
            messageSender.sendLocalizedText(chatId, "meme.error.ai");
        }
    }
    
    /**
     * Handle template selection
     */
    private void handleTemplateSelection(Long chatId, String text, UserSession session, User user) {
        // Check if user wants to go back
        if (text.equals("↩ Back")) {
            session.setState(UserState.IDLE);
            ReplyKeyboardMarkup mainMenuKeyboard = keyboardFactory.createMainMenuKeyboard();
            messageSender.sendLocalizedText(chatId, "welcome.action", mainMenuKeyboard);
            return;
        }
        
        // Проверка лимита для обычных пользователей
        if (memeService.hasReachedTemplateLimit(user)) {
            messageSender.sendLocalizedText(chatId, "meme.error.template.limit");
            return;
        }
        
        // Check if template exists
        List<String> templates = memeService.getAvailableTemplates();
        if (!templates.contains(text)) {
            messageSender.sendLocalizedText(chatId, "command.template.choose");
            return;
        }
        
        // Set template and update state
        session.setSelectedTemplate(text);
        session.setState(UserState.WAITING_FOR_TEMPLATE_TEXT);
        session.clearTemplateTextLines();
        
        // Send template text prompt
        messageSender.sendLocalizedText(chatId, "command.template.text", text);
    }
    
    /**
     * Handle template text
     */
    private void handleTemplateText(Long chatId, String text, UserSession session, User user) {
        // Split text into lines
        List<String> lines = Arrays.asList(text.split("\\n"));
        
        // Validate input
        ValidationResult validationResult = inputValidator.validateTemplateTextLines(lines);
        if (!validationResult.isValid()) {
            messageSender.sendText(chatId, validationResult.getErrorMessage());
            return;
        }
        
        // Send processing message
        messageSender.sendLocalizedText(chatId, "meme.generating.template");
        
        try {
            // Generate meme from template
            CompletableFuture<String> memeUrlFuture = memeService.generateMemeFromTemplate(
                    session.getSelectedTemplate(), 
                    lines,
                    user);
            
            memeUrlFuture.thenAccept(memeUrl -> {
                // Set session state and meme URL
                session.setState(UserState.MEME_GENERATED);
                session.setLastMemeUrl(memeUrl);
                
                // Send meme with actions keyboard
                ReplyKeyboardMarkup actionsKeyboard = keyboardFactory.createMemeActionKeyboard();
                messageSender.sendPhotoWithLocalizedCaption(
                        chatId, 
                        memeUrl, 
                        "meme.result.template", 
                        actionsKeyboard);
                
                logger.info("Generated template meme for chat ID: {}", chatId);
            }).exceptionally(e -> {
                logger.error("Error generating template meme", e);
                messageSender.sendLocalizedText(chatId, "meme.error.template");
                return null;
            });
        } catch (Exception e) {
            logger.error("Error generating template meme", e);
            messageSender.sendLocalizedText(chatId, "meme.error.template");
        }
    }
    
    /**
     * Handle meme action
     */
    private void handleMemeAction(Long chatId, String text, UserSession session, User user) {
        String memeUrl = session.getLastMemeUrl();
        
        if (memeUrl == null) {
            messageSender.sendLocalizedText(chatId, "common.error");
            session.setState(UserState.IDLE);
            return;
        }
        
        // Handle action based on text
        if (text.equals(messageService.getMemeActionPublishMessage())) {
            handlePublishAction(chatId, memeUrl, session, user);
        } else if (text.equals(messageService.getMemeActionContestMessage())) {
            handleContestAction(chatId, memeUrl, session, user);
        } else if (text.equals(messageService.getMemeActionNftMessage())) {
            handleNftAction(chatId, memeUrl, session, user);
        } else if (text.equals(messageService.getMemeActionNewMessage())) {
            handleNewMemeAction(chatId, session);
        } else {
            // Unknown action, treat as new AI description
            handleAiDescription(chatId, text, session, user);
        }
    }
    
    /**
     * Handle publish action
     */
    private void handlePublishAction(Long chatId, String memeUrl, UserSession session, User user) {
        boolean success = memeService.publishMemeToFeed(memeUrl, user.getTelegramId());
        
        if (success) {
            messageSender.sendLocalizedText(chatId, "meme.publish.success");
        } else {
            messageSender.sendLocalizedText(chatId, "meme.publish.error");
        }
        
        // Reset state
        session.setState(UserState.IDLE);
    }
    
    /**
     * Handle contest action
     */
    private void handleContestAction(Long chatId, String memeUrl, UserSession session, User user) {
        boolean success = contestService.submitMemeToContest(memeUrl, user.getTelegramId());
        
        if (success) {
            messageSender.sendLocalizedText(chatId, "meme.contest.success");
            
            // Отправить текущий статус конкурса
            String statusMessage = contestService.getContestStatusMessage();
            messageSender.sendText(chatId, statusMessage);
        } else {
            messageSender.sendLocalizedText(chatId, "meme.contest.error");
        }
        
        // Reset state
        session.setState(UserState.IDLE);
    }
    
    /**
     * Handle NFT action
     */
    private void handleNftAction(Long chatId, String memeUrl, UserSession session, User user) {
        String nftUrl = memeService.createNFT(memeUrl, user.getTelegramId());
        
        if (nftUrl != null) {
            messageSender.sendLocalizedText(chatId, "meme.nft.success", nftUrl);
        } else {
            messageSender.sendLocalizedText(chatId, "meme.nft.error");
        }
        
        // Reset state
        session.setState(UserState.IDLE);
    }
    
    /**
     * Handle new meme action
     */
    private void handleNewMemeAction(Long chatId, UserSession session) {
        // Reset state
        session.setState(UserState.IDLE);
        
        // Send main menu
        ReplyKeyboardMarkup mainMenuKeyboard = keyboardFactory.createMainMenuKeyboard();
        messageSender.sendLocalizedText(chatId, "welcome.action", mainMenuKeyboard);
    }

    // Обработка сообщений в админ-меню
    private void handleAdminMenuMessage(Message message, String text, UserSession session, User user) {
        Long chatId = message.getChatId();
        
        if (text.equals(messageService.getAdminUsersButtonMessage())) {
            session.setState(UserState.ADMIN_USERS_MENU);
            messageSender.sendLocalizedText(chatId, "admin.users.title", keyboardFactory.createUserManagementKeyboard());
        } else if (text.equals(messageService.getAdminStatsButtonMessage())) {
            // Получаем статистику
            int totalUsers = userService.getTotalUsers();
            int activeUsers = userService.getActiveUsers(7);
            int totalMemes = memeService.getTotalMemes();
            int todayMemes = memeService.getTodayMemes();
            
            String statsMessage = messageService.getMessage("admin.stats.message", 
                    totalUsers, activeUsers, totalMemes, todayMemes);
            messageSender.sendText(chatId, statsMessage);
        } else if (text.equals(messageService.getAdminSettingsButtonMessage())) {
            session.setState(UserState.ADMIN_SETTINGS_MENU);
            messageSender.sendLocalizedText(chatId, "admin.settings.title", keyboardFactory.createSettingsKeyboard());
        } else if (text.equals(messageService.getAdminBroadcastButtonMessage())) {
            session.setState(UserState.ADMIN_BROADCAST_COMPOSE);
            messageSender.sendLocalizedText(chatId, "admin.broadcast.prompt");
        } else if (text.equals(messageService.getAdminMaintenanceButtonMessage())) {
            boolean aiStatus = memeService.isAiEnabled();
            boolean voiceStatus = memeService.isVoiceEnabled();
            
            String statusMessage = messageService.getMessage("admin.maintenance.status", 
                    aiStatus ? "✅" : "❌", 
                    voiceStatus ? "✅" : "❌");
            messageSender.sendText(chatId, statusMessage);
        } else if (text.equals(messageService.getMessage("admin.contest.end"))) {
            // Завершение конкурса администратором
            String result = contestService.adminEndContest();
            messageSender.sendText(chatId, result);
        } else if (text.equals(messageService.getAdminBackButtonMessage())) {
            session.setState(UserState.IDLE);
            messageSender.sendLocalizedText(chatId, "welcome.action", keyboardFactory.createMainMenuKeyboard());
        } else {
            messageSender.sendLocalizedText(chatId, "admin.unknown.command");
        }
    }

    // Обработка сообщений в меню управления пользователями
    private void handleAdminUsersMenuMessage(Message message, String text, UserSession session, User user) {
        Long chatId = message.getChatId();
        
        if (text.equals(messageService.getMessage("admin.users.list"))) {
            List<User> topUsers = userService.getTopUsersByMemes();
            StringBuilder userList = new StringBuilder(messageService.getMessage("admin.users.list.title") + "\n\n");
            
            for (int i = 0; i < Math.min(topUsers.size(), 10); i++) {
                User topUser = topUsers.get(i);
                userList.append(i + 1).append(". ")
                       .append(topUser.getUsername() != null ? "@" + topUser.getUsername() : topUser.getFirstName())
                       .append(" - ").append(topUser.getTotalMemes()).append(" memes\n");
            }
            
            messageSender.sendText(chatId, userList.toString());
        } else if (text.equals(messageService.getMessage("admin.users.search"))) {
            session.setState(UserState.ADMIN_USER_SEARCH);
            messageSender.sendLocalizedText(chatId, "admin.users.search.prompt");
        } else if (text.equals(messageService.getMessage("admin.users.premium"))) {
            List<User> premiumUsers = userService.getPremiumUsers();
            StringBuilder userList = new StringBuilder(messageService.getMessage("admin.users.premium.title") + "\n\n");
            
            if (premiumUsers.isEmpty()) {
                userList.append(messageService.getMessage("admin.users.premium.empty"));
            } else {
                for (int i = 0; i < premiumUsers.size(); i++) {
                    User premiumUser = premiumUsers.get(i);
                    userList.append(i + 1).append(". ")
                           .append(premiumUser.getUsername() != null ? "@" + premiumUser.getUsername() : premiumUser.getFirstName())
                           .append("\n");
                }
            }
            
            messageSender.sendText(chatId, userList.toString());
        } else if (text.equals(messageService.getMessage("admin.users.inactive"))) {
            List<User> inactiveUsers = userService.getInactiveUsers(30);
            StringBuilder userList = new StringBuilder(messageService.getMessage("admin.users.inactive.title") + "\n\n");
            
            if (inactiveUsers.isEmpty()) {
                userList.append(messageService.getMessage("admin.users.inactive.empty"));
            } else {
                for (int i = 0; i < Math.min(inactiveUsers.size(), 20); i++) {
                    User inactiveUser = inactiveUsers.get(i);
                    userList.append(i + 1).append(". ")
                           .append(inactiveUser.getUsername() != null ? "@" + inactiveUser.getUsername() : inactiveUser.getFirstName())
                           .append(" - ").append(inactiveUser.getLastActivity()).append("\n");
                }
            }
            
            messageSender.sendText(chatId, userList.toString());
        } else if (text.equals(messageService.getAdminBackButtonMessage())) {
            session.setState(UserState.ADMIN_MENU);
            messageSender.sendLocalizedText(chatId, "admin.menu.title", keyboardFactory.createAdminMenuKeyboard());
        } else {
            messageSender.sendLocalizedText(chatId, "admin.unknown.command");
        }
    }

    // Обработка сообщений в меню настроек
    private void handleAdminSettingsMenuMessage(Message message, String text, UserSession session, User user) {
        Long chatId = message.getChatId();
        
        if (text.equals(messageService.getMessage("admin.settings.ai"))) {
            boolean currentStatus = memeService.isAiEnabled();
            memeService.setAiEnabled(!currentStatus);
            
            String statusMessage = messageService.getMessage("admin.settings.ai.toggled", 
                    !currentStatus ? "✅" : "❌");
            messageSender.sendText(chatId, statusMessage);
        } else if (text.equals(messageService.getMessage("admin.settings.voice"))) {
            boolean currentStatus = memeService.isVoiceEnabled();
            memeService.setVoiceEnabled(!currentStatus);
            
            String statusMessage = messageService.getMessage("admin.settings.voice.toggled", 
                    !currentStatus ? "✅" : "❌");
            messageSender.sendText(chatId, statusMessage);
        } else if (text.equals(messageService.getMessage("admin.settings.templates"))) {
            session.setState(UserState.ADMIN_TEMPLATE_MANAGEMENT);
            
            List<String> templates = memeService.getAvailableTemplates();
            StringBuilder templateList = new StringBuilder(messageService.getMessage("admin.templates.list") + "\n\n");
            
            for (int i = 0; i < templates.size(); i++) {
                templateList.append(i + 1).append(". ").append(templates.get(i)).append("\n");
            }
            
            templateList.append("\n").append(messageService.getMessage("admin.templates.add.prompt"));
            messageSender.sendText(chatId, templateList.toString());
        } else if (text.equals(messageService.getAdminBackButtonMessage())) {
            session.setState(UserState.ADMIN_MENU);
            messageSender.sendLocalizedText(chatId, "admin.menu.title", keyboardFactory.createAdminMenuKeyboard());
        } else {
            messageSender.sendLocalizedText(chatId, "admin.unknown.command");
        }
    }

    // Обработка сообщений при составлении рассылки
    private void handleAdminBroadcastComposeMessage(Message message, String text, UserSession session, User user) {
        Long chatId = message.getChatId();
        
        if (text.equals(messageService.getAdminBackButtonMessage())) {
            session.setState(UserState.ADMIN_MENU);
            messageSender.sendLocalizedText(chatId, "admin.menu.title", keyboardFactory.createAdminMenuKeyboard());
        } else {
            // Отправка сообщения всем пользователям
            List<User> allUsers = userService.getAllUsers();
            int sentCount = 0;
            
            messageSender.sendLocalizedText(chatId, "admin.broadcast.sending");
            
            for (User recipient : allUsers) {
                try {
                    messageSender.sendText(Long.parseLong(recipient.getTelegramId()), text);
                    sentCount++;
                    
                    // Небольшая задержка, чтобы не превысить лимиты Telegram API
                    Thread.sleep(50);
                } catch (Exception e) {
                    logger.error("Error sending broadcast to user: " + recipient.getTelegramId(), e);
                }
            }
            
            messageSender.sendLocalizedText(chatId, "admin.broadcast.sent", sentCount, allUsers.size());
            
            // Возвращаемся в админ-меню
            session.setState(UserState.ADMIN_MENU);
            messageSender.sendLocalizedText(chatId, "admin.menu.title", keyboardFactory.createAdminMenuKeyboard());
        }
    }

    // Обработка сообщений при поиске пользователя
    private void handleAdminUserSearchMessage(Message message, String text, UserSession session, User user) {
        Long chatId = message.getChatId();
        
        if (text.equals(messageService.getAdminBackButtonMessage())) {
            session.setState(UserState.ADMIN_USERS_MENU);
            messageSender.sendLocalizedText(chatId, "admin.users.title", keyboardFactory.createUserManagementKeyboard());
        } else {
            // Поиск пользователя по username или telegramId
            Optional<User> foundUser = userService.getUserByUsername(text);
            
            if (!foundUser.isPresent() && text.matches("\\d+")) {
                foundUser = userService.getUserByTelegramId(text);
            }
            
            if (foundUser.isPresent()) {
                User targetUser = foundUser.get();
                session.setState(UserState.ADMIN_USER_DETAIL);
                session.setData("targetUserId", targetUser.getTelegramId());
                
                StringBuilder userInfo = new StringBuilder();
                userInfo.append("👤 ").append(messageService.getMessage("admin.user.detail.title")).append("\n\n");
                userInfo.append("ID: ").append(targetUser.getTelegramId()).append("\n");
                userInfo.append(messageService.getMessage("admin.user.detail.username")).append(": @").append(targetUser.getUsername()).append("\n");
                userInfo.append(messageService.getMessage("admin.user.detail.name")).append(": ").append(targetUser.getFirstName());
                
                if (targetUser.getLastName() != null) {
                    userInfo.append(" ").append(targetUser.getLastName());
                }
                
                userInfo.append("\n");
                userInfo.append(messageService.getMessage("admin.user.detail.memes")).append(": ").append(targetUser.getTotalMemes()).append("\n");
                userInfo.append(messageService.getMessage("admin.user.detail.likes")).append(": ").append(targetUser.getTotalLikes()).append("\n");
                userInfo.append(messageService.getMessage("admin.user.detail.premium")).append(": ").append(targetUser.getIsPremium() ? "✅" : "❌").append("\n");
                userInfo.append(messageService.getMessage("admin.user.detail.admin")).append(": ").append(targetUser.getIsAdmin() ? "✅" : "❌").append("\n");
                userInfo.append(messageService.getMessage("admin.user.detail.registered")).append(": ").append(targetUser.getCreatedAt()).append("\n");
                userInfo.append(messageService.getMessage("admin.user.detail.lastActive")).append(": ").append(targetUser.getLastActivity()).append("\n\n");
                
                userInfo.append(messageService.getMessage("admin.user.detail.actions")).append(":\n");
                userInfo.append("1. ").append(messageService.getMessage("admin.user.action.togglePremium")).append("\n");
                userInfo.append("2. ").append(messageService.getMessage("admin.user.action.toggleAdmin")).append("\n");
                userInfo.append("3. ").append(messageService.getMessage("admin.user.action.delete")).append("\n");
                userInfo.append("4. ").append(messageService.getMessage("admin.user.action.back")).append("\n");
                
                messageSender.sendText(chatId, userInfo.toString());
            } else {
                messageSender.sendLocalizedText(chatId, "admin.users.search.notFound");
            }
        }
    }

    // Обработка сообщений в деталях пользователя
    private void handleAdminUserDetailMessage(Message message, String text, UserSession session, User user) {
        Long chatId = message.getChatId();
        String targetUserId = (String) session.getData("targetUserId");
        
        if (targetUserId == null) {
            session.setState(UserState.ADMIN_USERS_MENU);
            messageSender.sendLocalizedText(chatId, "admin.users.title", keyboardFactory.createUserManagementKeyboard());
            return;
        }
        
        if (text.equals("1") || text.equals(messageService.getMessage("admin.user.action.togglePremium"))) {
            Optional<User> targetUserOpt = userService.getUserByTelegramId(targetUserId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                boolean newStatus = !targetUser.getIsPremium();
                userService.setPremiumStatus(targetUserId, newStatus);
                
                String statusMessage = messageService.getMessage("admin.user.premium.toggled", 
                        targetUser.getUsername(), newStatus ? "✅" : "❌");
                messageSender.sendText(chatId, statusMessage);
            }
        } else if (text.equals("2") || text.equals(messageService.getMessage("admin.user.action.toggleAdmin"))) {
            Optional<User> targetUserOpt = userService.getUserByTelegramId(targetUserId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                boolean newStatus = !targetUser.getIsAdmin();
                userService.setAdminStatus(targetUserId, newStatus);
                
                String statusMessage = messageService.getMessage("admin.user.admin.toggled", 
                        targetUser.getUsername(), newStatus ? "✅" : "❌");
                messageSender.sendText(chatId, statusMessage);
            }
        } else if (text.equals("3") || text.equals(messageService.getMessage("admin.user.action.delete"))) {
            Optional<User> targetUserOpt = userService.getUserByTelegramId(targetUserId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                String username = targetUser.getUsername();
                
                userService.deleteUser(targetUserId);
                
                String statusMessage = messageService.getMessage("admin.user.deleted", username);
                messageSender.sendText(chatId, statusMessage);
                
                session.setState(UserState.ADMIN_USERS_MENU);
                messageSender.sendLocalizedText(chatId, "admin.users.title", keyboardFactory.createUserManagementKeyboard());
            }
        } else if (text.equals("4") || text.equals(messageService.getMessage("admin.user.action.back")) || 
                   text.equals(messageService.getAdminBackButtonMessage())) {
            session.setState(UserState.ADMIN_USERS_MENU);
            messageSender.sendLocalizedText(chatId, "admin.users.title", keyboardFactory.createUserManagementKeyboard());
        } else {
            messageSender.sendLocalizedText(chatId, "admin.unknown.command");
        }
    }

    // Обработка сообщений в управлении шаблонами
    private void handleAdminTemplateManagementMessage(Message message, String text, UserSession session, User user) {
        Long chatId = message.getChatId();
        
        if (text.equals(messageService.getAdminBackButtonMessage())) {
            session.setState(UserState.ADMIN_SETTINGS_MENU);
            messageSender.sendLocalizedText(chatId, "admin.settings.title", keyboardFactory.createSettingsKeyboard());
        } else if (text.startsWith("add:")) {
            // Добавление нового шаблона
            String templateName = text.substring(4).trim();
            if (!templateName.isEmpty()) {
                memeService.addTemplate(templateName);
                messageSender.sendLocalizedText(chatId, "admin.templates.added", templateName);
            }
        } else if (text.startsWith("remove:")) {
            // Удаление шаблона
            String templateName = text.substring(7).trim();
            if (!templateName.isEmpty()) {
                boolean removed = memeService.removeTemplate(templateName);
                if (removed) {
                    messageSender.sendLocalizedText(chatId, "admin.templates.removed", templateName);
                } else {
                    messageSender.sendLocalizedText(chatId, "admin.templates.notFound", templateName);
                }
            }
        } else {
            // Предполагаем, что это имя нового шаблона
            memeService.addTemplate(text);
            messageSender.sendLocalizedText(chatId, "admin.templates.added", text);
        }
    }
} 