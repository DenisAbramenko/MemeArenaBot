package org.abr.memearenabot.repository;

import org.abr.memearenabot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entities
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by Telegram ID
     */
    Optional<User> findByTelegramId(String telegramId);
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find users by premium status
     */
    List<User> findByIsPremium(Boolean isPremium);
    
    /**
     * Find premium users
     */
    List<User> findByIsPremiumIsTrue();
    
    /**
     * Find admin users
     */
    List<User> findByIsAdminIsTrue();
    
    /**
     * Find top users by total memes
     */
    List<User> findTop10ByOrderByTotalMemesDesc();
    
    /**
     * Find top users by total likes
     */
    List<User> findTop10ByOrderByTotalLikesDesc();
    
    /**
     * Find inactive users
     */
    List<User> findByLastActivityBefore(LocalDateTime dateTime);
    
    /**
     * Find active users
     */
    List<User> findByLastActivityAfter(LocalDateTime dateTime);
    
    /**
     * Check if user exists by Telegram ID
     */
    boolean existsByTelegramId(String telegramId);
} 