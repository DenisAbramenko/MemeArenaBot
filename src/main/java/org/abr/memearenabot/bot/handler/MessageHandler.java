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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Handler for user messages
 */
public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final MemeService memeService;
    private final UserService userService;
    private final MessageService messageService;
    private final KeyboardFactory keyboardFactory;
    private final MessageSender messageSender;
    private final InputValidator inputValidator;
    private final ContestService contestService;

    public MessageHandler(TelegramBot bot, MemeService memeService, UserService userService,
                          MessageService messageService, KeyboardFactory keyboardFactory, MessageSender messageSender
            , InputValidator inputValidator, ContestService contestService) {
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

        // Handle Help button click from main menu
        if (text.equals(messageService.getKeyboardHelpMessage())) {
            Long chatId = message.getChatId();
            handleHelpButtonClick(chatId, session);
            return;
        }

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
        }

        // Handle text based on current state
        switch (state) {
            case WAITING_FOR_AI_DESCRIPTION:
                handleAiDescription(message.getChatId(), text, session, user);
                break;
            case MEME_GENERATED:
                handleMemeAction(message.getChatId(), text, session, user);
                break;
            case WAITING_FOR_LOGIN:
                // In WAITING_FOR_LOGIN state, handle login button clicks
                if (text.equals(messageService.getMessage("Войти как пользователь"))) {
                    handleUserLogin(message.getChatId(), session, user);
                } else if (text.equals(messageService.getMessage("Войти как админ"))) {
                    handleAdminLogin(message.getChatId(), session, user);
                } else {
                    // Unknown input in login state, prompt for login again
                    messageSender.sendText(message.getChatId(), "Пожалуйста, выберите вариант входа:",
                            keyboardFactory.createLoginKeyboard());
                }
                break;
            case WAITING_FOR_ADMIN_PASSWORD:
                // Handle admin password verification
                handleAdminPasswordVerification(message.getChatId(), text, session, user);
                break;
            case IDLE:
                // In IDLE state, check if this is a button action first
                if (text.equals(messageService.getKeyboardAiMessage())) {
                    // Handle AI button click - set state and prompt for description
                    session.setState(UserState.WAITING_FOR_AI_DESCRIPTION);
                    messageSender.sendLocalizedText(message.getChatId(), "command.ai.prompt");
                } else if (text.equals(messageService.getMessage("keyboard.back"))) {
                    // Handle Back button in main menu - stay in main menu
                    ReplyKeyboardMarkup mainMenuKeyboard = keyboardFactory.createMainMenuKeyboard();
                    messageSender.sendLocalizedText(message.getChatId(), "welcome.action", mainMenuKeyboard);
                } else if (text.equals(messageService.getKeyboardContestMessage())) {
                    // Handle Contest button click
                    messageSender.sendLocalizedText(message.getChatId(), "command.contest.info");
                    String statusMessage = contestService.getContestStatusMessage();
                    messageSender.sendText(message.getChatId(), statusMessage);
                } else {
                    // If not a recognized button, treat as AI description
                    handleAiDescription(message.getChatId(), text, session, user);
                }
                break;
            default:
                // In other states, treat text as AI description
                handleAiDescription(message.getChatId(), text, session, user);
                break;
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
                messageSender.sendPhotoWithLocalizedCaption(chatId, memeUrl, "meme.result.ai", actionsKeyboard);

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
            messageSender.sendLocalizedText(chatId, "admin.users.title",
                    keyboardFactory.createUserManagementKeyboard());
        } else if (text.equals(messageService.getAdminStatsButtonMessage())) {
            // Получаем статистику
            int totalUsers = userService.getTotalUsers();
            int activeUsers = userService.getActiveUsers(7);
            int totalMemes = memeService.getTotalMemes();
            int todayMemes = memeService.getTodayMemes();

            String statsMessage = messageService.getMessage("admin.stats.message", totalUsers, activeUsers,
                    totalMemes, todayMemes);
            messageSender.sendText(chatId, statsMessage);
        } else if (text.equals(messageService.getAdminSettingsButtonMessage())) {
            session.setState(UserState.ADMIN_SETTINGS_MENU);
            messageSender.sendLocalizedText(chatId, "admin.settings.title", keyboardFactory.createSettingsKeyboard());
        } else if (text.equals(messageService.getAdminBroadcastButtonMessage())) {
            session.setState(UserState.ADMIN_BROADCAST_COMPOSE);
            messageSender.sendLocalizedText(chatId, "admin.broadcast.prompt");
        } else if (text.equals(messageService.getAdminMaintenanceButtonMessage())) {
            boolean aiStatus = memeService.isAiEnabled();
            String statusMessage = messageService.getMessage("admin.maintenance.status", aiStatus ? "✅" : "❌");
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
                userList.append(i + 1).append(". ").append(topUser.getUsername() != null ?
                        "@" + topUser.getUsername() : topUser.getFirstName()).append(" - ").append(topUser.getTotalMemes()).append(" memes\n");
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
                    userList.append(i + 1).append(". ").append(premiumUser.getUsername() != null ?
                            "@" + premiumUser.getUsername() : premiumUser.getFirstName()).append("\n");
                }
            }

            messageSender.sendText(chatId, userList.toString());
        } else if (text.equals(messageService.getMessage("admin.users.inactive"))) {
            List<User> inactiveUsers = userService.getInactiveUsers(30);
            StringBuilder userList =
                    new StringBuilder(messageService.getMessage("admin.users.inactive.title") + "\n" + "\n");

            if (inactiveUsers.isEmpty()) {
                userList.append(messageService.getMessage("admin.users.inactive.empty"));
            } else {
                for (int i = 0; i < Math.min(inactiveUsers.size(), 20); i++) {
                    User inactiveUser = inactiveUsers.get(i);
                    userList.append(i + 1).append(". ").append(inactiveUser.getUsername() != null ?
                            "@" + inactiveUser.getUsername() : inactiveUser.getFirstName()).append(" - ").append(inactiveUser.getLastActivity()).append("\n");
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

            String statusMessage = messageService.getMessage("admin.settings.ai.toggled", !currentStatus ? "✅" : "❌");
            messageSender.sendText(chatId, statusMessage);
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
            messageSender.sendLocalizedText(chatId, "admin.users.title",
                    keyboardFactory.createUserManagementKeyboard());
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
                userInfo.append("1. ").append(messageService.getMessage("admin.user.action.togglePremium")).append(
                        "\n");
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
            messageSender.sendLocalizedText(chatId, "admin.users.title",
                    keyboardFactory.createUserManagementKeyboard());
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

                String statusMessage = messageService.getMessage("admin.user.admin.toggled", targetUser.getUsername()
                        , newStatus ? "✅" : "❌");
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
                messageSender.sendLocalizedText(chatId, "admin.users.title",
                        keyboardFactory.createUserManagementKeyboard());
            }
        } else if (text.equals("4") || text.equals(messageService.getMessage("admin.user.action.back")) || text.equals(messageService.getAdminBackButtonMessage())) {
            session.setState(UserState.ADMIN_USERS_MENU);
            messageSender.sendLocalizedText(chatId, "admin.users.title",
                    keyboardFactory.createUserManagementKeyboard());
        } else {
            messageSender.sendLocalizedText(chatId, "admin.unknown.command");
        }
    }

    /**
     * Handle Help button click
     */
    private void handleHelpButtonClick(Long chatId, UserSession session) {
        logger.info("Handling Help button click for chat ID: {}", chatId);

        try {
            // Build help message similar to CommandHandler.buildHelpMessage()

            // Add information about limits and premium
            String helpText = messageService.getHelpMessage() + "\n\n" + messageService.getMessage("help.limits") + "\n\n" + messageService.getMessage("help.premium");

            // Send help message with main menu keyboard
            ReplyKeyboardMarkup mainMenuKeyboard = keyboardFactory.createMainMenuKeyboard();
            messageSender.sendText(chatId, helpText, mainMenuKeyboard);

            logger.debug("Sent help message to chat ID: {}", chatId);
        } catch (Exception e) {
            logger.error("Error sending help message to chat ID: {}", chatId, e);
            messageSender.sendLocalizedText(chatId, "common.error");
        }
    }

    private void handleUserLogin(Long chatId, UserSession session, User user) {
        logger.info("User login selected for user with ID: {}", user.getTelegramId());

        // Mark user as logged in
        session.setState(UserState.IDLE);

        // Send welcome message for regular user with main menu keyboard
        String welcomeMessage = "👤 Вы вошли как обычный пользователь.\n\n" + "Вы можете создавать мемы и участвовать " +
                "в конкурсах";

        messageSender.sendText(chatId, welcomeMessage, keyboardFactory.createMainMenuKeyboard());
    }

    private void handleAdminLogin(Long chatId, UserSession session, User user) {
        logger.info("Admin login attempted for user with ID: {}", user.getTelegramId());

        // Check if user already has admin rights in database
        if (userService.isAdmin(user.getTelegramId())) {
            // Set admin state
            session.setState(UserState.ADMIN_MENU);

            // Send admin welcome message with admin keyboard
            messageSender.sendLocalizedText(chatId, "admin.welcome", keyboardFactory.createAdminMenuKeyboard());
        } else {
            // Ask for admin password
            session.setState(UserState.WAITING_FOR_ADMIN_PASSWORD);
            messageSender.sendText(chatId, "Введите пароль администратора:");
        }
    }

    /**
     * Checks the admin password and grants admin access if correct
     */
    private void handleAdminPasswordVerification(Long chatId, String password, UserSession session, User user) {
        try {
            // Don't log sensitive data
            logger.info("Admin password verification attempt for user ID: {}", user.getTelegramId());

            // Try service method first
            boolean isValid = userService.verifyAdminPassword(password);

            if (isValid) {
                // Set user as admin in database
                userService.setAdminStatus(user.getTelegramId(), true);

                // Set admin state
                session.setState(UserState.ADMIN_MENU);

                // Send admin welcome message with admin keyboard
                messageSender.sendLocalizedText(chatId, "admin.welcome", keyboardFactory.createAdminMenuKeyboard());
                logger.info("Admin access granted for user ID: {}", user.getTelegramId());
            } else {
                // Send access denied message and go back to login state
                session.setState(UserState.WAITING_FOR_LOGIN);
                messageSender.sendLocalizedText(chatId, "admin.access.denied", keyboardFactory.createLoginKeyboard());
                logger.warn("Admin access denied for user ID: {} - incorrect password", user.getTelegramId());
            }
        } catch (Exception e) {
            // If there's an error reading the admin password, deny access
            logger.error("Error verifying admin password: {}", e.getMessage(), e);
            session.setState(UserState.WAITING_FOR_LOGIN);
            messageSender.sendLocalizedText(chatId, "admin.access.denied", keyboardFactory.createLoginKeyboard());
        }
    }
} 