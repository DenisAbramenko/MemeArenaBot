package org.abr.memearenabot.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class DotenvConfig {

    private static final Logger logger = LoggerFactory.getLogger(DotenvConfig.class);

    @PostConstruct
    public void loadEnv() {
        File envFile = new File(".env");

        if (envFile.exists()) {
            logger.info("Loading configuration from .env file");

            try {
                Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

                // Установка переменных окружения из .env
                dotenv.entries().forEach(entry -> {
                    if (System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });

                logger.info("Environment variables loaded successfully from .env file");
            } catch (Exception e) {
                logger.error("Failed to load .env file: {}", e.getMessage(), e);
            }
        } else {
            logger.info(".env file not found, using system environment variables");
        }
    }
} 