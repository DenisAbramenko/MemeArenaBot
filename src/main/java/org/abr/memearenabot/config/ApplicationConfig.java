package org.abr.memearenabot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Основная конфигурация приложения
 */
@Configuration
@EnableRetry
@EnableAsync
public class ApplicationConfig {
    // Здесь могут быть определены бины конфигурации
} 