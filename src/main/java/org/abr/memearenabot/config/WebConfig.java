package org.abr.memearenabot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web конфигурация для доступа к статическим ресурсам
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${meme.storage.path:./meme-storage}")
    private String memeStoragePath;

    /**
     * Настройка обработчиков ресурсов для доступа к изображениям мемов
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Получаем абсолютный путь к директории хранения мемов
        Path memeStorageLocation = Paths.get(memeStoragePath).toAbsolutePath().normalize();
        String memeStorageLocationPath = memeStorageLocation.toString().replace("\\", "/");

        // Регистрируем обработчик для доступа к изображениям
        registry.addResourceHandler("/images/**").addResourceLocations("file:" + memeStorageLocationPath + "/");
    }
} 