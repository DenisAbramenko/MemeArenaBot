package org.abr.memearenabot.bot.handler;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.bot.keyboard.KeyboardFactory;
import org.abr.memearenabot.bot.session.UserSession;
import org.abr.memearenabot.bot.session.UserState;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.service.ContestService;
import org.abr.memearenabot.service.MemeService;
import org.abr.memearenabot.service.MessageService;
import org.abr.memearenabot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Handler for bot commands
 */
public class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private static final String LOG_PREFIX = "Command: ";

    // Command constants
    private static final String CMD_START = "/start";
    private static final String CMD_HELP = "/help";
    private static final String CMD_AI = "/ai";
    private static final String CMD_CONTEST = "/contest";
    private static final String CMD_PREMIUM = "/premium";
    private static final String CMD_ADMIN = "/admin";

    private final TelegramBot bot;
    private final UserService userService;
    private final MessageService messageService;
    private final KeyboardFactory keyboardFactory;
    private final ContestService contestService;

    public CommandHandler(TelegramBot bot, UserService userService,
                          MessageService messageService, KeyboardFactory keyboardFactory,
                          ContestService contestService) {
        this.bot = bot;
        this.userService = userService;
        this.messageService = messageService;
        this.keyboardFactory = keyboardFactory;
        this.contestService = contestService;
        logger.debug("{}Handler initialized", LOG_PREFIX);
    }

    /**
     * Handle bot commands
     *
     * @param message Telegram message containing command
     * @param text    Command text
     * @param session User session
     * @param user    User who sent the command
     */
    public void handleCommand(Message message, String text, UserSession session, User user) {
        Long chatId = message.getChatId();
        String command = text.split(" ")[0].toLowerCase();

        logger.debug("{}Handling command '{}' from chat ID: {}", LOG_PREFIX, command, chatId);

        // Store last command
        session.setLastCommand(command);

        // Handle admin command separately
        if (command.startsWith(CMD_ADMIN)) {
            handleAdminCommand(chatId, session, user);
            return;
        }

        // Handle other commands
        switch (command) {
            case CMD_START:
                handleStartCommand(chatId, session);
                break;
            case CMD_HELP:
                handleHelpCommand(chatId);
                break;
            case CMD_AI:
                handleAiCommand(chatId, session);
                break;
            case CMD_CONTEST:
                handleContestCommand(chatId, session);
                break;
            case CMD_PREMIUM:
                handlePremiumCommand(chatId, session, user);
                break;
            default:
                handleUnknownCommand(chatId, session);
                break;
        }
    }

    /**
     * Handle admin command
     */
    private void handleAdminCommand(Long chatId, UserSession session, User user) {
        logger.info("{}Processing admin command from chat ID: {}", LOG_PREFIX, chatId);

        if (userService.isAdmin(user.getTelegramId())) {
            session.setState(UserState.ADMIN_MENU);

            try {
                SendMessage adminMessage = createMessage(chatId, messageService.getAdminWelcomeMessage(),
                        keyboardFactory.createAdminMenuKeyboard());

                bot.execute(adminMessage);
                logger.debug("{}Sent admin welcome message to chat ID: {}", LOG_PREFIX, chatId);
            } catch (TelegramApiException e) {
                logger.error("{}Error sending admin welcome message to chat ID: {}", LOG_PREFIX, chatId, e);
            }
        } else {
            try {
                SendMessage accessDeniedMessage = createMessage(chatId, messageService.getMessage("admin.access" +
                        ".denied"));

                bot.execute(accessDeniedMessage);
                logger.debug("{}Sent access denied message to chat ID: {}", LOG_PREFIX, chatId);
            } catch (TelegramApiException e) {
                logger.error("{}Error sending access denied message to chat ID: {}", LOG_PREFIX, chatId, e);
            }
        }
    }

    /**
     * Handle /start command
     */
    private void handleStartCommand(Long chatId, UserSession session) {
        logger.info("{}Handling /start command for chat ID: {}", LOG_PREFIX, chatId);

        // Reset session state
        session.reset();

        // Set initial login state
        session.setState(UserState.WAITING_FOR_LOGIN);

        try {
            // Send welcome message with login keyboard
            SendMessage welcomeMessage = createMessage(chatId, messageService.getWelcomeMessage(),
                    keyboardFactory.createLoginKeyboard());
            bot.execute(welcomeMessage);

            logger.debug("{}Sent welcome message with login options to chat ID: {}", LOG_PREFIX, chatId);
        } catch (TelegramApiException e) {
            logger.error("{}Error sending welcome message to chat ID: {}", LOG_PREFIX, chatId, e);
        }
    }

    /**
     * Handle /help command
     */
    private void handleHelpCommand(Long chatId) {
        logger.info("{}Handling /help command for chat ID: {}", LOG_PREFIX, chatId);

        try {
            // Build help message
            StringBuilder helpText = buildHelpMessage();

            // Send help message with main menu keyboard
            SendMessage message = createMessage(chatId, helpText.toString(), keyboardFactory.createMainMenuKeyboard());

            bot.execute(message);
            logger.debug("{}Sent help message to chat ID: {}", LOG_PREFIX, chatId);
        } catch (TelegramApiException e) {
            logger.error("{}Error sending help message to chat ID: {}", LOG_PREFIX, chatId, e);
        }
    }

    /**
     * Build help message with all sections
     */
    private StringBuilder buildHelpMessage() {
        StringBuilder helpText = new StringBuilder(messageService.getHelpMessage());

        // Add information about limits and premium
        helpText.append("\n\n");
        helpText.append(messageService.getMessage("help.limits"));
        helpText.append("\n\n");
        helpText.append(messageService.getMessage("help.premium"));

        return helpText;
    }

    /**
     * Handle /ai command
     */
    private void handleAiCommand(Long chatId, UserSession session) {
        logger.info("{}Handling /ai command for chat ID: {}", LOG_PREFIX, chatId);

        // Set session state
        session.setState(UserState.WAITING_FOR_AI_DESCRIPTION);

        try {
            // Send prompt message
            SendMessage message = createMessage(chatId, messageService.getAiPromptMessage());

            bot.execute(message);
            logger.debug("{}Sent AI prompt message to chat ID: {}", LOG_PREFIX, chatId);
        } catch (TelegramApiException e) {
            logger.error("{}Error sending AI prompt message to chat ID: {}", LOG_PREFIX, chatId, e);
        }
    }

    /**
     * Handle /contest command
     */
    private void handleContestCommand(Long chatId, UserSession session) {
        logger.info("{}Handling /contest command for chat ID: {}", LOG_PREFIX, chatId);

        try {
            // Send contest info message
            SendMessage message = createMessage(chatId, messageService.getContestInfoMessage());
            bot.execute(message);

            // Send current contest status
            String statusMessage = contestService.getContestStatusMessage();
            SendMessage statusMsg = createMessage(chatId, statusMessage);

            bot.execute(statusMsg);
            logger.debug("{}Sent contest info and status messages to chat ID: {}", LOG_PREFIX, chatId);
        } catch (TelegramApiException e) {
            logger.error("{}Error sending contest info message to chat ID: {}", LOG_PREFIX, chatId, e);
        }
    }

    /**
     * Handle /premium command
     */
    private void handlePremiumCommand(Long chatId, UserSession session, User user) {
        logger.info("{}Handling /premium command for chat ID: {}", LOG_PREFIX, chatId);

        try {
            // Send premium info message based on user status
            String messageKey = user.getIsPremium() ? "premium.active" : "premium.benefits";
            SendMessage message = createMessage(chatId, messageService.getMessage(messageKey));

            bot.execute(message);
            logger.debug("{}Sent premium info message to chat ID: {}", LOG_PREFIX, chatId);
        } catch (TelegramApiException e) {
            logger.error("{}Error sending premium info message to chat ID: {}", LOG_PREFIX, chatId, e);
        }
    }

    /**
     * Handle unknown command
     */
    private void handleUnknownCommand(Long chatId, UserSession session) {
        logger.info("{}Handling unknown command for chat ID: {}", LOG_PREFIX, chatId);

        try {
            // Send unknown command message
            SendMessage message = createMessage(chatId, messageService.getUnknownCommandMessage());

            bot.execute(message);
            logger.debug("{}Sent unknown command message to chat ID: {}", LOG_PREFIX, chatId);
        } catch (TelegramApiException e) {
            logger.error("{}Error sending unknown command message to chat ID: {}", LOG_PREFIX, chatId, e);
        }
    }

    /**
     * Create a SendMessage with text
     */
    private SendMessage createMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    /**
     * Create a SendMessage with text and keyboard
     */
    private SendMessage createMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(keyboard);
        return message;
    }
} 