package org.abr.memearenabot.scheduler;

import org.abr.memearenabot.bot.TelegramBot;
import org.abr.memearenabot.bot.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scheduler for cleaning up expired user sessions
 */
@Component
public class SessionCleanupScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SessionCleanupScheduler.class);

    private final TelegramBot telegramBot;

    @Autowired
    public SessionCleanupScheduler(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * Clean up expired sessions every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupExpiredSessions() {
        try {
            logger.info("Starting cleanup of expired sessions");

            Map<Long, UserSession> sessions = telegramBot.getUserSessions();
            int initialSize = sessions.size();

            // Find expired sessions
            Map<Long, UserSession> expiredSessions =
                    sessions.entrySet().stream().filter(entry -> entry.getValue().isExpired(30)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // Remove expired sessions
            expiredSessions.keySet().forEach(sessions::remove);

            int removedCount = expiredSessions.size();
            logger.info("Session cleanup completed. Removed {} expired sessions out of {}. Current session count: {}"
                    , removedCount, initialSize, sessions.size());
        } catch (Exception e) {
            logger.error("Error during session cleanup", e);
        }
    }
} 