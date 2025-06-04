package org.abr.memearenabot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableScheduling
public class MemeArenaBotApplication {

    public static void main(String[] args) {
        // Загрузка .env файла перед запуском приложения
        loadEnvFile();
        
        // Запуск приложения Spring Boot
        SpringApplication.run(MemeArenaBotApplication.class, args);
    }
    
    private static void loadEnvFile() {
        File envFile = new File(".env");
        
        if (envFile.exists()) {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            // Установка переменных окружения из .env
            dotenv.entries().forEach(entry -> {
                if (System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        }
    }
}
