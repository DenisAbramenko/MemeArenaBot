package org.abr.memearenabot.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;

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
        cacheManager.setCacheNames(Arrays.asList(
            "users", 
            "memeTemplates", 
            "topMemes", 
            "contestMemes", 
            "userMemes",
            "topUsersByMemes",
            "topUsersByLikes"
        ));
        return cacheManager;
    }
    
    /**
     * Clear user caches every 12 hours
     */
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // 12 hours
    public void clearUserCaches() {
        cacheManager().getCache("users").clear();
        cacheManager().getCache("topUsersByMemes").clear();
        cacheManager().getCache("topUsersByLikes").clear();
    }
    
    /**
     * Clear meme caches every 6 hours
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours
    public void clearMemeCaches() {
        cacheManager().getCache("topMemes").clear();
        cacheManager().getCache("contestMemes").clear();
        cacheManager().getCache("userMemes").clear();
    }
    
    /**
     * Clear template cache every 24 hours
     */
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24 hours
    public void clearTemplateCaches() {
        cacheManager().getCache("memeTemplates").clear();
    }
} 