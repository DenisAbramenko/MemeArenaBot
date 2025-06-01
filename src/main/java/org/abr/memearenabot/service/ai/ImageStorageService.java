package org.abr.memearenabot.service.ai;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import javax.imageio.ImageIO;

/**
 * Сервис для хранения изображений
 */
@Service
@Slf4j
public class ImageStorageService {
    private static final String LOG_PREFIX = "[Storage] ";
    private static final String DEFAULT_IMAGE_FORMAT = "png";
    private static final String URL_PREFIX = "url";
    private static final String BASE64_PREFIX = "base64";
    private static final String BYTES_PREFIX = "bytes";
    private static final int UUID_LENGTH = 12;

    @Value("${meme.storage.path:./meme-storage}")
    private String storageBasePath;

    @Value("${meme.storage.url:http://localhost:8080/images/}")
    private String storageBaseUrl;

    /**
     * Инициализация хранилища
     */
    @PostConstruct
    public void init() {
        try {
            ensureStorageDirectoryExists();
            log.info("{}Initialized with path: {}", LOG_PREFIX, storageBasePath);
        } catch (IOException e) {
            log.error("{}Failed to initialize image storage: {}", LOG_PREFIX, e.getMessage(), e);
            throw new StorageException("Failed to initialize image storage", e);
        }
    }

    /**
     * Сохраняет изображение из URL
     *
     * @param imageUrl URL изображения
     * @return URL сохраненного изображения
     * @throws StorageException если произошла ошибка при сохранении
     */
    public String saveImageFromUrl(String imageUrl) {
        validateInput(imageUrl, "Image URL cannot be null", "Image URL cannot be empty");

        try {
            log.info("{}Saving image from URL: {}", LOG_PREFIX, imageUrl);

            String fileName = generateFileName(URL_PREFIX, DEFAULT_IMAGE_FORMAT);
            Path targetPath = getTargetPath(fileName);

            downloadFile(imageUrl, targetPath);

            String resultUrl = getResultUrl(fileName);
            log.info("{}Image saved to: {}", LOG_PREFIX, targetPath);
            return resultUrl;
        } catch (IOException e) {
            log.error("{}Error saving image from URL: {}", LOG_PREFIX, e.getMessage(), e);
            throw new StorageException("Error saving image from URL", e);
        }
    }

    /**
     * Сохраняет изображение из Base64
     *
     * @param base64Image Base64 строка изображения
     * @return URL сохраненного изображения
     * @throws StorageException если произошла ошибка при сохранении
     */
    public String saveBase64Image(String base64Image) {
        validateInput(base64Image, "Base64 image cannot be null", "Base64 image cannot be empty");

        try {
            log.info("{}Saving image from Base64", LOG_PREFIX);

            String fileName = generateFileName(BASE64_PREFIX, DEFAULT_IMAGE_FORMAT);
            Path targetPath = getTargetPath(fileName);

            // Декодирование Base64 и сохранение
            byte[] imageBytes = decodeBase64(base64Image);
            Files.write(targetPath, imageBytes);

            String resultUrl = getResultUrl(fileName);
            log.info("{}Image saved to: {}", LOG_PREFIX, targetPath);
            return resultUrl;
        } catch (IllegalArgumentException e) {
            log.error("{}Invalid Base64 format: {}", LOG_PREFIX, e.getMessage(), e);
            throw new StorageException("Invalid Base64 format", e);
        } catch (IOException e) {
            log.error("{}Error saving Base64 image: {}", LOG_PREFIX, e.getMessage(), e);
            throw new StorageException("Error saving Base64 image", e);
        }
    }

    /**
     * Декодирует Base64 строку в массив байтов
     */
    private byte[] decodeBase64(String base64Image) {
        try {
            return Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            throw new StorageException("Failed to decode Base64 string: " + e.getMessage(), e);
        }
    }

    /**
     * Сохраняет изображение из массива байтов
     *
     * @param imageBytes    Массив байтов изображения
     * @param fileExtension Расширение файла (png, jpg, etc.)
     * @return URL сохраненного изображения
     * @throws StorageException если произошла ошибка при сохранении
     */
    public String saveImageBytes(byte[] imageBytes, String fileExtension) {
        Objects.requireNonNull(imageBytes, "Image bytes cannot be null");

        if (imageBytes.length == 0) {
            throw new IllegalArgumentException("Image bytes cannot be empty");
        }

        String extension = StringUtils.hasText(fileExtension) ? fileExtension : DEFAULT_IMAGE_FORMAT;

        try {
            log.info("{}Saving image from bytes, size: {} bytes", LOG_PREFIX, imageBytes.length);

            String fileName = generateFileName(BYTES_PREFIX, extension);
            Path targetPath = getTargetPath(fileName);

            Files.write(targetPath, imageBytes);

            String resultUrl = getResultUrl(fileName);
            log.info("{}Image saved to: {}", LOG_PREFIX, targetPath);
            return resultUrl;
        } catch (IOException e) {
            log.error("{}Error saving image bytes: {}", LOG_PREFIX, e.getMessage(), e);
            throw new StorageException("Error saving image bytes", e);
        }
    }

    /**
     * Удаляет изображение по URL
     *
     * @param imageUrl URL изображения
     * @return true если удаление успешно
     */
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("{}Attempt to delete null or empty URL", LOG_PREFIX);
            return false;
        }

        try {
            log.info("{}Deleting image: {}", LOG_PREFIX, imageUrl);

            String fileName = extractFileNameFromUrl(imageUrl);
            if (fileName == null) {
                log.warn("{}Cannot extract filename from URL: {}", LOG_PREFIX, imageUrl);
                return false;
            }

            Path targetPath = getTargetPath(fileName);
            if (!Files.exists(targetPath)) {
                log.warn("{}Image not found: {}", LOG_PREFIX, targetPath);
                return false;
            }

            Files.delete(targetPath);
            log.info("{}Image deleted: {}", LOG_PREFIX, targetPath);
            return true;
        } catch (IOException e) {
            log.error("{}Error deleting image: {}", LOG_PREFIX, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Загружает файл из URL в указанный путь
     *
     * @param fileUrl    URL файла
     * @param targetPath Путь для сохранения
     * @throws IOException если произошла ошибка при загрузке
     */
    private void downloadFile(String fileUrl, Path targetPath) throws IOException {
        try {
            URL url = new URL(fileUrl);
            try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream()); FileOutputStream fileOutputStream = new FileOutputStream(targetPath.toFile())) {
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }
        } catch (IOException e) {
            // Try to use a local fallback image instead of failing
            log.warn("{}Failed to download from URL: {}. Using fallback image.", LOG_PREFIX, fileUrl, e);
            try {
                // Create a simple colored image as fallback
                createFallbackImage(targetPath);
                return;
            } catch (Exception fallbackEx) {
                log.error("{}Failed to create fallback image", LOG_PREFIX, fallbackEx);
                throw new IOException("Failed to download file from URL: " + fileUrl, e);
            }
        }
    }

    /**
     * Creates a simple fallback image when remote image download fails
     * 
     * @param targetPath Path to save the fallback image
     * @throws IOException if unable to create the fallback image
     */
    private void createFallbackImage(Path targetPath) throws IOException {
        // Create a simple 600x400 solid color image with text
        int width = 600;
        int height = 400;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // Set background color (light gray)
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);
        
        // Draw a border
        g.setColor(Color.DARK_GRAY);
        g.drawRect(5, 5, width - 10, height - 10);
        
        // Draw text
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String message = "Image Unavailable";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        g.drawString(message, (width - textWidth) / 2, height / 2);
        
        g.dispose();
        
        // Write the image to the target path
        try (OutputStream out = new FileOutputStream(targetPath.toFile())) {
            ImageIO.write(image, "png", out);
        }
        
        log.info("{}Created fallback image at: {}", LOG_PREFIX, targetPath);
    }

    /**
     * Проверяет входные параметры
     */
    private void validateInput(String input, String nullMessage, String emptyMessage) {
        Objects.requireNonNull(input, nullMessage);

        if (input.trim().isEmpty()) {
            throw new IllegalArgumentException(emptyMessage);
        }
    }

    /**
     * Получает полный путь к файлу в хранилище
     *
     * @param fileName Имя файла
     * @return Полный путь к файлу
     */
    private Path getTargetPath(String fileName) {
        return Paths.get(storageBasePath, fileName);
    }

    /**
     * Получает URL доступа к сохраненному изображению
     *
     * @param fileName Имя файла
     * @return URL доступа к изображению
     */
    private String getResultUrl(String fileName) {
        return storageBaseUrl + fileName;
    }

    /**
     * Генерирует имя файла
     *
     * @param prefix    Префикс для имени файла
     * @param extension Расширение файла
     * @return Сгенерированное имя файла
     */
    private String generateFileName(String prefix, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, UUID_LENGTH);
        return prefix + "-" + uuid + "." + extension;
    }

    /**
     * Извлекает имя файла из URL
     *
     * @param url URL
     * @return Имя файла или null, если не удалось извлечь
     */
    private String extractFileNameFromUrl(String url) {
        if (url.startsWith(storageBaseUrl)) {
            return url.substring(storageBaseUrl.length());
        }
        return null;
    }

    /**
     * Убеждается, что директория для хранения существует
     *
     * @throws IOException если не удалось создать директорию
     */
    private void ensureStorageDirectoryExists() throws IOException {
        Path storagePath = Paths.get(storageBasePath);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
    }

    /**
     * Исключение для ошибок хранилища изображений
     */
    public static class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }

        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 