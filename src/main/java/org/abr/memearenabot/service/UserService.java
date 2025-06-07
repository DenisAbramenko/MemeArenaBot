package org.abr.memearenabot.service;

import org.abr.memearenabot.model.User;
import org.abr.memearenabot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing users
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get user by Telegram ID, create if not exists
     */
    @Transactional
    public User getOrCreateUser(String telegramId, String username, String firstName, String lastName,
                                String languageCode) {
        return userRepository.findByTelegramId(telegramId).map(user -> {
            // Update user data if changed
            boolean updated = false;

            if (username != null && !username.equals(user.getUsername())) {
                user.setUsername(username);
                updated = true;
            }

            if (firstName != null && !firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                updated = true;
            }

            if (lastName != null && !lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                updated = true;
            }

            if (languageCode != null && !languageCode.equals(user.getLanguageCode())) {
                user.setLanguageCode(languageCode);
                updated = true;
            }

            // Update activity
            user.updateActivity();

            if (updated) {
                logger.debug("Updated user data for Telegram ID: {}", telegramId);
            }

            return userRepository.save(user);
        }).orElseGet(() -> {
            User newUser = new User(telegramId, username, firstName, lastName, languageCode);
            logger.info("Created new user with Telegram ID: {}", telegramId);
            return userRepository.save(newUser);
        });
    }

    /**
     * Get user by Telegram ID, create if not exists from Message
     */
    @Transactional
    public User getOrCreateUser(Message message) {
        org.telegram.telegrambots.meta.api.objects.User telegramUser = message.getFrom();
        return getOrCreateUser(telegramUser.getId().toString(), telegramUser.getUserName(),
                telegramUser.getFirstName(), telegramUser.getLastName(), telegramUser.getLanguageCode());
    }

    /**
     * Get user by Telegram ID
     */
    @Cacheable(value = "users", key = "#telegramId")
    public Optional<User> getUserByTelegramId(String telegramId) {
        logger.debug("Fetching user with Telegram ID: {}", telegramId);
        return userRepository.findByTelegramId(telegramId);
    }

    /**
     * Get user by username
     */
    @Cacheable(value = "users", key = "#username")
    public Optional<User> getUserByUsername(String username) {
        logger.debug("Fetching user with username: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * Update user activity asynchronously
     */
    @Async
    @CacheEvict(value = "users", key = "#telegramId")
    public CompletableFuture<Void> updateUserActivity(String telegramId) {
        return CompletableFuture.runAsync(() -> {
            userRepository.findByTelegramId(telegramId).ifPresent(user -> {
                user.updateActivity();
                userRepository.save(user);
                logger.debug("Updated activity for user with Telegram ID: {}", telegramId);
            });
        });
    }

    /**
     * Set premium status for user
     */
    @Transactional
    @CacheEvict(value = "users", key = "#telegramId")
    public boolean setPremiumStatus(String telegramId, boolean isPremium) {
        return userRepository.findByTelegramId(telegramId).map(user -> {
            user.setIsPremium(isPremium);
            userRepository.save(user);
            logger.info("Set premium status to {} for user with Telegram ID: {}", isPremium, telegramId);
            return true;
        }).orElse(false);
    }

    /**
     * Get top users by memes count
     */
    @Cacheable(value = "topUsersByMemes")
    public List<User> getTopUsersByMemes() {
        logger.debug("Fetching top users by memes count");
        return userRepository.findTop10ByOrderByTotalMemesDesc();
    }

    /**
     * Get top users by likes count
     */
    @Cacheable(value = "topUsersByLikes")
    public List<User> getTopUsersByLikes() {
        logger.debug("Fetching top users by likes count");
        return userRepository.findTop10ByOrderByTotalLikesDesc();
    }

    /**
     * Get inactive users
     */
    public List<User> getInactiveUsers(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        logger.debug("Fetching users inactive since {}", cutoff);
        return userRepository.findByLastActivityBefore(cutoff);
    }

    /**
     * Delete user by Telegram ID
     */
    @Transactional
    @CacheEvict(value = "users", key = "#telegramId")
    public boolean deleteUser(String telegramId) {
        return userRepository.findByTelegramId(telegramId).map(user -> {
            userRepository.delete(user);
            logger.info("Deleted user with Telegram ID: {}", telegramId);
            return true;
        }).orElse(false);
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin(String telegramId) {
        Optional<User> userOpt = getUserByTelegramId(telegramId);
        return userOpt.map(User::getIsAdmin).orElse(false);
    }

    /**
     * Set admin status for user
     */
    public boolean setAdminStatus(String telegramId, boolean isAdmin) {
        Optional<User> userOpt = getUserByTelegramId(telegramId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsAdmin(isAdmin);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Get total number of users
     */
    public int getTotalUsers() {
        return (int) userRepository.count();
    }

    /**
     * Get number of active users within the last N days
     */
    public int getActiveUsers(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return userRepository.findByLastActivityAfter(cutoff).size();
    }

    /**
     * Get list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get list of premium users
     */
    public List<User> getPremiumUsers() {
        return userRepository.findByIsPremiumIsTrue();
    }

    /**
     * Verify admin password against configuration
     */
    public boolean verifyAdminPassword(String password) {
        // Get admin password from environment variable (highest priority)
        String configuredPassword = System.getenv("ADMIN_PASSWORD");

        // Don't log sensitive values in production
        logger.debug("Checking admin password from environment variables");

        if (configuredPassword == null || configuredPassword.isEmpty()) {
            // Fallback to a hardcoded password hash - not ideal but better than plaintext
            // In a production environment, you would use a proper password hashing algorithm
            // This is a simple hash of "adminpass123" 
            final String HARDCODED_PASSWORD_HASH = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";

            // Hash the input password for comparison (using SHA-256)
            String hashedInput = hashPassword(password);

            // Compare the hashed values
            return hashedInput.equals(HARDCODED_PASSWORD_HASH);
        }

        // Direct comparison for environment variable password
        return password != null && password.equals(configuredPassword);
    }

    /**
     * Simple password hashing using SHA-256
     * In a production system, use a more secure approach with salt
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            return "";
        }
    }
} 