package org.abr.memearenabot.bot.handler;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.bot.sender.MessageSender;
import org.abr.memearenabot.bot.session.UserSession;
import org.abr.memearenabot.bot.session.UserState;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.service.MemeService;
import org.abr.memearenabot.service.MessageService;
import org.abr.memearenabot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Handler for callback queries from inline keyboards
 */
public class CallbackHandler {
    private static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    private final MemeService memeService;
    private final UserService userService;
    private final MessageService messageService;
    private final MessageSender messageSender;
    private final TelegramBot bot;

    public CallbackHandler(MemeService memeService, UserService userService, MessageService messageService,
                           MessageSender messageSender, TelegramBot bot) {
        this.memeService = memeService;
        this.userService = userService;
        this.messageService = messageService;
        this.messageSender = messageSender;
        this.bot = bot;
    }

    /**
     * Handle callback queries
     */
    public void handleCallback(CallbackQuery callbackQuery, UserSession session, User user) {
        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        logger.debug("Handling callback with data: {}", callbackData);

        try {
            // Answer callback query to stop loading animation
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            bot.execute(answer);

            // Parse callback data
            String[] parts = callbackData.split(":");
            String action = parts[0];

            switch (action) {
                case "publish":
                    handlePublishCallback(chatId, parts, session, user);
                    break;
                case "contest":
                    handleContestCallback(chatId, parts, session, user);
                    break;
                case "vote":
                    handleVoteCallback(chatId, parts, session, user);
                    break;
                case "page":
                    handlePageCallback(chatId, parts, session, user);
                    break;
                case "new":
                case "back":
                    handleBackCallback(chatId, session);
                    break;
                default:
                    messageSender.sendLocalizedText(chatId, "common.error");
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling callback query", e);
            try {
                messageSender.sendLocalizedText(chatId, "common.error");
            } catch (Exception ex) {
                logger.error("Error sending error message", ex);
            }
        }
    }

    /**
     * Handle publish callback
     */
    private void handlePublishCallback(Long chatId, String[] parts, UserSession session, User user) throws TelegramApiException {
        if (parts.length < 2) {
            messageSender.sendLocalizedText(chatId, "common.error");
            return;
        }

        String memeUrl = parts[1];
        boolean success = memeService.publishMemeToFeed(memeUrl, user.getTelegramId());

        if (success) {
            messageSender.sendLocalizedText(chatId, "meme.publish.success");
        } else {
            messageSender.sendLocalizedText(chatId, "meme.publish.error");
        }
    }

    /**
     * Handle contest callback
     */
    private void handleContestCallback(Long chatId, String[] parts, UserSession session, User user) throws TelegramApiException {
        if (parts.length < 2) {
            messageSender.sendLocalizedText(chatId, "common.error");
            return;
        }

        String memeUrl = parts[1];
        boolean success = memeService.submitMemeToContest(memeUrl, user.getTelegramId());

        if (success) {
            messageSender.sendLocalizedText(chatId, "meme.contest.success");
        } else {
            messageSender.sendLocalizedText(chatId, "meme.contest.error");
        }
    }

    /**
     * Handle vote callback
     */
    private void handleVoteCallback(Long chatId, String[] parts, UserSession session, User user) throws TelegramApiException {
        if (parts.length < 2) {
            messageSender.sendLocalizedText(chatId, "common.error");
            return;
        }

        try {
            Long memeId = Long.parseLong(parts[1]);
            boolean success = memeService.voteMeme(memeId);

            if (success) {
                messageSender.sendText(chatId, "👍 Спасибо за ваш голос!");
            } else {
                messageSender.sendText(chatId, "Не удалось проголосовать. Возможно, вы уже голосовали за этот мем.");
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid meme ID in vote callback", e);
            messageSender.sendLocalizedText(chatId, "common.error");
        }
    }

    /**
     * Handle pagination callback
     */
    private void handlePageCallback(Long chatId, String[] parts, UserSession session, User user) throws TelegramApiException {
        if (parts.length < 3) {
            messageSender.sendLocalizedText(chatId, "common.error");
            return;
        }

        String pageType = parts[1];
        int page;

        try {
            page = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            logger.error("Invalid page number in pagination callback", e);
            messageSender.sendLocalizedText(chatId, "common.error");
            return;
        }

        // Handle different page types (e.g., contest, templates, user memes)
        switch (pageType) {
            case "contest":
                showContestPage(chatId, page);
                break;
            case "memes":
                showUserMemesPage(chatId, user, page);
                break;
            default:
                messageSender.sendLocalizedText(chatId, "common.error");
                break;
        }
    }

    /**
     * Handle back/new callback
     */
    private void handleBackCallback(Long chatId, UserSession session) throws TelegramApiException {
        // Reset state
        session.setState(UserState.IDLE);

        // Send welcome action message with main menu
        messageSender.sendLocalizedText(chatId, "welcome.action");
    }

    /**
     * Show contest page
     */
    private void showContestPage(Long chatId, int page) {
        // Implementation for showing contest memes with pagination
        // This would fetch contest memes for the specified page and display them
    }

    /**
     * Show user memes page
     */
    private void showUserMemesPage(Long chatId, User user, int page) {
        // Implementation for showing user memes with pagination
        // This would fetch user memes for the specified page and display them
    }
} 