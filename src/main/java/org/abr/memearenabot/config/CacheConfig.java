package org.abr.memearenabot.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.Objects;

/**
 * Configuration for caching
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    /**
     * Cache manager bean
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList("users", "memeTemplates", "topMemes", "contestMemes", "userMemes",
                "topUsersByMemes", "topUsersByLikes"));
        return cacheManager;
    }

    /**
     * Clear user caches every 12 hours
     */
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // 12 hours
    public void clearUserCaches() {
        Objects.requireNonNull(cacheManager().getCache("users")).clear();
        Objects.requireNonNull(cacheManager().getCache("topUsersByMemes")).clear();
        Objects.requireNonNull(cacheManager().getCache("topUsersByLikes")).clear();
    }

    /**
     * Clear meme caches every 6 hours
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours
    public void clearMemeCaches() {
        Objects.requireNonNull(cacheManager().getCache("topMemes")).clear();
        Objects.requireNonNull(cacheManager().getCache("contestMemes")).clear();
        Objects.requireNonNull(cacheManager().getCache("userMemes")).clear();
    }

    /**
     * Clear template cache every 24 hours
     */
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24 hours
    public void clearTemplateCaches() {
        Objects.requireNonNull(cacheManager().getCache("memeTemplates")).clear();
    }
} 