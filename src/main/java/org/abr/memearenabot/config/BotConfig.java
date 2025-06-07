package org.abr.memearenabot.config;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.service.ContestService;
import org.abr.memearenabot.service.MemeService;
import org.abr.memearenabot.service.MessageService;
import org.abr.memearenabot.service.UserService;
import org.abr.memearenabot.validation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@EnableScheduling
public class BotConfig {
    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Bean
    public TelegramBot telegramBot(MemeService memeService, UserService userService, MessageService messageService,
                                   ContestService contestService, InputValidator inputValidator) {
        logger.info("Initializing Telegram bot with username: {}", botUsername);
        return new TelegramBot(memeService, userService, messageService, contestService, inputValidator);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) {
        try {
            logger.info("Registering Telegram bot with Telegram API");
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(telegramBot);
            logger.info("Telegram bot successfully registered");
            return api;
        } catch (TelegramApiRequestException e) {
            logger.error("Failed to register bot due to request exception: {}", e.getApiResponse(), e);
            throw new BotRegistrationException("Failed to register bot due to API request error", e);
        } catch (TelegramApiException e) {
            logger.error("Failed to register bot with Telegram API", e);
            throw new BotRegistrationException("Failed to register bot with Telegram API", e);
        }
    }

    /**
     * Custom exception for bot registration failures
     */
    public static class BotRegistrationException extends RuntimeException {
        public BotRegistrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 