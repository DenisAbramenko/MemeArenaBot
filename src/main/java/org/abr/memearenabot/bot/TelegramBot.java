package org.abr.memearenabot.bot;

import org.abr.memearenabot.bot.handler.CallbackHandler;
import org.abr.memearenabot.bot.handler.CommandHandler;
import org.abr.memearenabot.bot.handler.MessageHandler;
import org.abr.memearenabot.bot.keyboard.InlineKeyboardFactory;
import org.abr.memearenabot.bot.keyboard.KeyboardFactory;
import org.abr.memearenabot.bot.sender.MessageSender;
import org.abr.memearenabot.bot.session.UserSession;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.service.ContestService;
import org.abr.memearenabot.service.MemeService;
import org.abr.memearenabot.service.MessageService;
import org.abr.memearenabot.service.UserService;
import org.abr.memearenabot.validation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private MemeService memeService;
    private UserService userService;
    private MessageService messageService;
    private ContestService contestService;
    private InputValidator inputValidator;
    private CommandHandler commandHandler;
    private MessageHandler messageHandler;
    private KeyboardFactory keyboardFactory;
    private InlineKeyboardFactory inlineKeyboardFactory;
    private MessageSender messageSender;
    private CallbackHandler callbackHandler;

    // User session states
    private final Map<Long, UserSession> userSessions = new ConcurrentHashMap<>();

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    public TelegramBot(
            @Lazy MemeService memeService,
            @Lazy UserService userService,
            @Lazy MessageService messageService,
            @Lazy ContestService contestService,
            @Lazy InputValidator inputValidator) {
        // Default constructor that will be called by Spring
        // The token will be injected later, but we need to provide it to the parent constructor
        super("");
        this.memeService = memeService;
        this.userService = userService;
        this.messageService = messageService;
        this.contestService = contestService;
        this.inputValidator = inputValidator;
        logger.info("TelegramBot constructor called with dependencies");
    }

    @PostConstruct
    public void init() {
        // Initialize handlers and factories after all dependencies are injected
        this.messageSender = new MessageSender(this, messageService);
        this.keyboardFactory = new KeyboardFactory(this, messageService);
        this.inlineKeyboardFactory = new InlineKeyboardFactory(this, messageService);
        this.commandHandler = new CommandHandler(this, memeService, userService, messageService, keyboardFactory,
                contestService);
        this.messageHandler = new MessageHandler(this, memeService, userService, messageService, keyboardFactory,
                messageSender, inputValidator, contestService);
        this.callbackHandler = new CallbackHandler(memeService, userService, messageService, messageSender, this);
        
        // Re-initialize the parent's token field
        if (botToken != null && !botToken.isEmpty()) {
            try {
                java.lang.reflect.Field tokenField = TelegramLongPollingBot.class.getDeclaredField("token");
                tokenField.setAccessible(true);
                tokenField.set(this, botToken);
                logger.info("TelegramBot initialized with token: {}", botToken.substring(0, Math.min(botToken.length(), 5)) + "...");
            } catch (Exception e) {
                logger.error("Failed to set bot token", e);
            }
        }
        
        logger.info("TelegramBot fully initialized with all dependencies");
    }

    // Constructor for tests
    public TelegramBot(String testToken, MemeService memeService, UserService userService, MessageService messageService, 
                      InputValidator inputValidator, MemeService memeService1, UserService userService1, 
                      MessageService messageService1, ContestService contestService, InputValidator inputValidator1, 
                      CommandHandler commandHandler, MessageHandler messageHandler, KeyboardFactory keyboardFactory, 
                      InlineKeyboardFactory inlineKeyboardFactory, MessageSender messageSender, CallbackHandler callbackHandler) {
        super(testToken);
        this.memeService = memeService1;
        this.userService = userService1;
        this.messageService = messageService1;
        this.contestService = contestService;
        this.inputValidator = inputValidator1;
        this.commandHandler = commandHandler;
        this.messageHandler = messageHandler;
        this.keyboardFactory = keyboardFactory;
        this.inlineKeyboardFactory = inlineKeyboardFactory;
        this.messageSender = messageSender;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        logger.debug("Received {} updates", updates.size());
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        // This method is still needed for backward compatibility
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            // Handle callback queries from inline keyboards
            if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                Long chatId = callbackQuery.getMessage().getChatId();

                logger.debug("Received callback query from chat ID: {}", chatId);

                // Initialize session if it doesn't exist
                if (!userSessions.containsKey(chatId)) {
                    userSessions.put(chatId, new UserSession());
                    logger.debug("Created new session for chat ID: {}", chatId);
                }

                // Get or create user
                User user;
                MaybeInaccessibleMessage maybeMessage = callbackQuery.getMessage();
                if (maybeMessage instanceof Message) {
                    user = userService.getOrCreateUser((Message) maybeMessage);
                } else {
                    // If message is inaccessible, use the user from the callback query
                    org.telegram.telegrambots.meta.api.objects.User telegramUser = callbackQuery.getFrom();
                    user = userService.getOrCreateUser(telegramUser.getId().toString(), telegramUser.getUserName(),
                            telegramUser.getFirstName(), telegramUser.getLastName(), telegramUser.getLanguageCode());
                }

                // Update user activity asynchronously
                userService.updateUserActivity(user.getTelegramId());

                UserSession session = userSessions.get(chatId);
                callbackHandler.handleCallback(callbackQuery, session, user);
                return;
            }

            // Handle messages
            if (update.hasMessage()) {
                Message message = update.getMessage();
                Long chatId = message.getChatId();

                logger.debug("Received message from chat ID: {}", chatId);

                // Initialize session if it doesn't exist
                if (!userSessions.containsKey(chatId)) {
                    userSessions.put(chatId, new UserSession());
                    logger.debug("Created new session for chat ID: {}", chatId);
                }

                // Get or create user
                User user = userService.getOrCreateUser(message);

                // Update user activity asynchronously
                userService.updateUserActivity(user.getTelegramId());

                UserSession session = userSessions.get(chatId);

                // Handle voice messages
                if (message.hasVoice()) {
                    logger.info("Processing voice message from chat ID: {}", chatId);
                    messageHandler.handleVoiceMessage(message, session, user);
                    return;
                }

                // Handle text messages
                if (message.hasText()) {
                    String text = message.getText();
                    logger.debug("Processing text message: '{}' from chat ID: {}", text, chatId);

                    // Handle commands
                    if (text.startsWith("/")) {
                        logger.info("Processing command: '{}' from chat ID: {}", text, chatId);
                        commandHandler.handleCommand(message, text, session, user);
                        return;
                    }

                    // Process message based on current state
                    messageHandler.handleTextMessage(message, text, session, user);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing update", e);
        }
    }

    // Getters for session management
    public Map<Long, UserSession> getUserSessions() {
        return userSessions;
    }

    public MemeService getMemeService() {
        return memeService;
    }

    public UserService getUserService() {
        return userService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public InputValidator getInputValidator() {
        return inputValidator;
    }
}
