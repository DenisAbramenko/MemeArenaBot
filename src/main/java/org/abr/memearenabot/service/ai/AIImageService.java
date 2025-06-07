package org.abr.memearenabot.service.ai;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для интеграции с API генерации изображений на основе ИИ
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIImageService {
    private static final String LOG_PREFIX = "[AI-Service] ";
    private static final int DEFAULT_TIMEOUT_MS = 30000;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;
    private static final double RETRY_MULTIPLIER = 2.0;

    // Параметры для Stability AI
    private static final int STABILITY_CFG_SCALE = 7;
    private static final int STABILITY_HEIGHT = 1024;
    private static final int STABILITY_WIDTH = 1024;
    private static final int STABILITY_SAMPLES = 1;
    private static final int STABILITY_STEPS = 30;
    private static final double STABILITY_TEXT_WEIGHT = 1.0;
    private final ImageStorageService imageStorageService;
    private RestTemplate restTemplate;
    @Value("${ai.stability.api-key:}")
    private String stabilityAiApiKey;

    @Value("${ai.stability.url:https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image}")
    private String stabilityAiUrl;

    @PostConstruct
    public void init() {
        log.info("{}Initializing AI Image Service", LOG_PREFIX);

        // Настройка RestTemplate
        setupRestTemplate();

        if (isStabilityAiConfigured()) {
            log.info("{}Stability AI API configured", LOG_PREFIX);
        } else {
            log.warn("{}Stability AI API not configured", LOG_PREFIX);
        }
    }

    /**
     * Настраивает RestTemplate
     */
    private void setupRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(DEFAULT_TIMEOUT_MS);
        requestFactory.setReadTimeout(DEFAULT_TIMEOUT_MS);

        restTemplate = new RestTemplate(requestFactory);
        log.info("{}RestTemplate configured", LOG_PREFIX);
    }

    /**
     * Генерирует мем на основе описания
     *
     * @param description Описание мема
     * @return URL сгенерированного мема
     */
    @Async
    public CompletableFuture<String> generateMeme(String description) {
        Objects.requireNonNull(description, "Description cannot be null");

        if (description.trim().isEmpty()) {
            return CompletableFuture.failedFuture(new AIServiceException("Description cannot be empty"));
        }

        log.info("{}Generating meme with description: {}", LOG_PREFIX, description);
        String memePrompt = enhanceMemePrompt(description);

        if (isStabilityAiConfigured()) {
            try {
                return generateImageWithStabilityAI(memePrompt);
            } catch (AIServiceException e) {
                log.error("{}Failed to generate with Stability AI: {}", LOG_PREFIX, e.getMessage());
                return CompletableFuture.completedFuture(getFallbackImageUrl(description));
            }
        }

        // Если API не настроен, вернуть заглушку
        log.warn("{}No AI image generation API configured, using fallback", LOG_PREFIX);
        return CompletableFuture.completedFuture(getFallbackImageUrl(description));
    }

    /**
     * Генерирует изображение с помощью Stability AI
     *
     * @param prompt Текстовое описание для генерации изображения
     * @return URL сгенерированного изображения
     */
    @Retryable(value = {RestClientException.class}, maxAttempts = MAX_RETRY_ATTEMPTS, backoff = @Backoff(delay =
            INITIAL_RETRY_DELAY_MS, multiplier = RETRY_MULTIPLIER))
    private CompletableFuture<String> generateImageWithStabilityAI(String prompt) {
        log.info("{}Generating image with Stability AI", LOG_PREFIX);

        if (!isStabilityAiConfigured()) {
            throw new AIServiceException("Stability AI API key is not configured");
        }

        try {
            HttpHeaders headers = createStabilityAiHeaders();
            Map<String, Object> requestBody = createStabilityAiRequestBody(prompt);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(stabilityAiUrl, request, Map.class);

            return handleStabilityAiResponse(response);
        } catch (RestClientException e) {
            log.error("{}RestClient error with Stability API: {}", LOG_PREFIX, e.getMessage());
            throw new AIServiceException("Error communicating with Stability AI API", e);
        } catch (Exception e) {
            log.error("{}Unexpected error with Stability API", LOG_PREFIX, e);
            throw new AIServiceException("Unexpected error with Stability AI API: " + e.getMessage(), e);
        }
    }

    /**
     * Создает заголовки для запроса к Stability AI
     */
    private HttpHeaders createStabilityAiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + stabilityAiApiKey);
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * Создает тело запроса для Stability AI
     */
    private Map<String, Object> createStabilityAiRequestBody(String prompt) {
        Map<String, Object> textPrompt = new HashMap<>();
        textPrompt.put("text", prompt);
        textPrompt.put("weight", STABILITY_TEXT_WEIGHT);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text_prompts", List.of(textPrompt));
        requestBody.put("cfg_scale", STABILITY_CFG_SCALE);
        requestBody.put("height", STABILITY_HEIGHT);
        requestBody.put("width", STABILITY_WIDTH);
        requestBody.put("samples", STABILITY_SAMPLES);
        requestBody.put("steps", STABILITY_STEPS);
        return requestBody;
    }

    /**
     * Обрабатывает ответ от Stability AI
     */
    private CompletableFuture<String> handleStabilityAiResponse(ResponseEntity<Map> response) {
        if (response.getBody() == null) {
            log.error("{}Empty response from Stability AI", LOG_PREFIX);
            throw new AIServiceException("Empty response from Stability AI");
        }

        if (!response.getBody().containsKey("artifacts")) {
            log.error("{}Unexpected Stability AI response format: {}", LOG_PREFIX, response.getBody());
            throw new AIServiceException("Unexpected Stability AI response format");
        }

        try {
            List<Map<String, Object>> artifacts = (List<Map<String, Object>>) response.getBody().get("artifacts");
            if (artifacts.isEmpty() || !artifacts.get(0).containsKey("base64")) {
                log.error("{}No base64 image in Stability AI response", LOG_PREFIX);
                throw new AIServiceException("No base64 image in Stability AI response");
            }

            String base64Image = (String) artifacts.get(0).get("base64");
            String imageUrl = imageStorageService.saveBase64Image(base64Image);
            log.info("{}Successfully generated image with Stability AI", LOG_PREFIX);
            return CompletableFuture.completedFuture(imageUrl);
        } catch (ClassCastException e) {
            log.error("{}Error parsing Stability AI response: {}", LOG_PREFIX, e.getMessage());
            throw new AIServiceException("Error parsing Stability AI response", e);
        }
    }

    /**
     * Улучшает запрос для генерации мема
     */
    private String enhanceMemePrompt(String description) {
        return "Create a funny meme image based on this idea: " + description + ". If the text is not in English, " +
                "translate it to English first. Make it humorous, with vibrant colors, in a modern meme style.";
    }

    /**
     * Возвращает URL изображения-заглушки
     */
    private String getFallbackImageUrl(String description) {
        return "https://via.placeholder.com/1024x1024.png?text=" + description.replaceAll("\\s+", "+").substring(0,
                Math.min(description.length(), 30));
    }

    /**
     * Проверяет, настроен ли Stability AI API
     */
    private boolean isStabilityAiConfigured() {
        return stabilityAiApiKey != null && !stabilityAiApiKey.trim().isEmpty();
    }

    /**
     * Исключение для ошибок в сервисе ИИ
     */
    public static class AIServiceException extends RuntimeException {
        public AIServiceException(String message) {
            super(message);
        }

        public AIServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 
