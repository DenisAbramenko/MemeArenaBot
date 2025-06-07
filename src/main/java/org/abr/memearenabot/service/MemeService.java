package org.abr.memearenabot.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.abr.memearenabot.model.Meme;
import org.abr.memearenabot.model.User;
import org.abr.memearenabot.repository.MemeRepository;
import org.abr.memearenabot.repository.UserRepository;
import org.abr.memearenabot.service.ai.AIImageService;
import org.abr.memearenabot.service.ai.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemeService {
    private static final String LOG_PREFIX = "[Meme-Service] ";
    private static final int FREE_USER_AI_MEME_DAILY_LIMIT = 1;

    private final Random random = new Random();
    private final MemeRepository memeRepository;
    private final UserRepository userRepository;
    private final AIImageService aiImageService;
    private final ImageStorageService imageStorageService;

    private ContestService contestService;
    @Value("${meme.storage.url:https://meme-storage.com/memes/}")
    private String memeStorageUrl;
    // Флаги для управления функциональностью
    @Getter
    @Setter
    private boolean aiEnabled = true;

    @Autowired
    public void setContestService(ContestService contestService) {
        this.contestService = contestService;
    }

    /**
     * Generate AI MEME
     *
     * @param description Описание мема
     * @param user        Пользователь
     * @return URL сгенерированного мема
     * @throws MemeGenerationException если возникла ошибка при генерации
     */
    @Async
    @Transactional
    public CompletableFuture<String> generateMeme(String description, User user) {
        validateAiMemeGeneration(description, user);

        try {
            log.info("{}Generating AI meme for user {}: {}", LOG_PREFIX, user.getTelegramId(), description);

            // Использование AIImageService для генерации изображения
            return aiImageService.generateMeme(description).thenApply(imageUrl -> processGeneratedAiMeme(imageUrl,
                    description, user));
        } catch (Exception e) {
            log.error("{}Error generating AI meme: {}", LOG_PREFIX, e.getMessage(), e);
            throw new MemeGenerationException("Failed to generate AI meme", e);
        }
    }

    /**
     * Обрабатывает сгенерированный ИИ мем
     */
    private String processGeneratedAiMeme(String imageUrl, String description, User user) {
        try {
            // Сохранение изображения локально
            String localImageUrl = imageStorageService.saveImageFromUrl(imageUrl);

            // Создание объекта мема с явной установкой всех обязательных полей
            Meme meme = new Meme(localImageUrl, description, user);
            meme.setType(Meme.MemeType.AI_GENERATED);
            meme.setLikes(0);

            // Сохранение в базу данных
            memeRepository.save(meme);

            // Добавление мема в коллекцию пользователя
            user.addMeme(meme);
            userRepository.save(user);

            log.debug("{}Generated AI meme with ID: {}", LOG_PREFIX, meme.getId());
            return localImageUrl;
        } catch (Exception e) {
            log.error("{}Error processing AI meme: {}", LOG_PREFIX, e.getMessage(), e);
            throw new MemeGenerationException("Error processing AI meme", e);
        }
    }

    /**
     * Проверяет возможность генерации ИИ мема
     */
    private void validateAiMemeGeneration(String description, User user) {
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(user, "User cannot be null");

        if (!aiEnabled) {
            log.warn("{}AI meme generation is disabled", LOG_PREFIX);
            throw new MemeGenerationException("AI meme generation is currently disabled", null);
        }

        if (hasReachedAiLimit(user)) {
            log.warn("{}User {} has reached daily AI limit", LOG_PREFIX, user.getTelegramId());
            throw new MemeGenerationException("Daily AI generation limit reached", null);
        }
    }

    /**
     * Генерирует мем с помощью ИИ (упрощенный метод)
     *
     * @param description Описание мема
     * @return URL сгенерированного мема
     * @throws MemeGenerationException если возникла ошибка при генерации
     */
    @Transactional
    public String generateMeme(String description) {
        Objects.requireNonNull(description, "Description cannot be null");

        if (!aiEnabled) {
            log.warn("{}AI meme generation is disabled", LOG_PREFIX);
            throw new MemeGenerationException("AI meme generation is currently disabled", null);
        }

        try {
            log.info("{}Generating AI meme: {}", LOG_PREFIX, description);

            // Использование AIImageService для генерации изображения (блокирующий вызов)
            String imageUrl = aiImageService.generateMeme(description).join();

            // Сохранение изображения локально
            String localImageUrl = imageStorageService.saveImageFromUrl(imageUrl);

            // Создание временного ID пользователя
            String tempUserId = generateTempUserId();

            // Сохранение в базу данных
            Meme meme = new Meme(localImageUrl, description, tempUserId);
            meme.setType(Meme.MemeType.AI_GENERATED);
            meme.setLikes(0);
            meme.setInContest(false);
            memeRepository.save(meme);

            log.debug("{}Generated AI meme with ID: {}", LOG_PREFIX, meme.getId());
            return localImageUrl;
        } catch (Exception e) {
            log.error("{}Error generating AI meme: {}", LOG_PREFIX, e.getMessage(), e);
            throw new MemeGenerationException("Failed to generate AI meme", e);
        }
    }

    /**
     * Публикует мем в ленту (URL)
     *
     * @param memeUrl URL мема
     * @param userId  ID пользователя
     * @return true если публикация успешна
     */
    @Transactional
    public boolean publishMemeToFeed(String memeUrl, String userId) {
        validateParams(memeUrl, userId, "Invalid parameters for publishing to feed");

        return findMemeByUrl(memeUrl).map(meme -> {
            publishMemeToFeed(meme, userId);
            return true;
        }).orElseGet(() -> {
            log.warn("{}Meme not found for URL: {}", LOG_PREFIX, memeUrl);
            return false;
        });
    }

    /**
     * Публикует мем в ленту
     */
    private void publishMemeToFeed(Meme meme, String userId) {
        // Здесь может быть логика публикации в ленту, оповещения других пользователей и т.д.
        meme.setPublishedToFeed(true);
        meme.setPublishedAt(LocalDateTime.now());
        memeRepository.save(meme);

        log.info("{}Meme {} published to feed by user {}", LOG_PREFIX, meme.getId(), userId);
    }

    /**
     * Создает мем и добавляет его пользователю
     */
    @Transactional
    protected Meme createMeme(String imageUrl, String description, User user, Meme.MemeType type) {
        Meme meme = new Meme(imageUrl, description, user);
        meme.setType(type);

        // Явная проверка и установка поля likes
        if (meme.getLikes() == null) {
            meme.setLikes(0);
        }

        // Явная установка поля inContest
        meme.setInContest(false);

        memeRepository.save(meme);

        // Добавление мема в коллекцию пользователя
        user.addMeme(meme);
        userRepository.save(user);

        return meme;
    }

    /**
     * Проверяет параметры запроса
     */
    private void validateParams(String memeUrl, String userId, String errorMessage) {
        if (!StringUtils.hasText(memeUrl) || !StringUtils.hasText(userId)) {
            log.warn("{}{}", LOG_PREFIX, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Ищет мем по URL
     */
    private Optional<Meme> findMemeByUrl(String url) {
        // Оптимизированный метод поиска мема по URL
        return memeRepository.findAll().stream().filter(meme -> Objects.equals(url, meme.getImageUrl())).findFirst();
    }

    /**
     * Генерирует временный ID пользователя
     */
    private String generateTempUserId() {
        return "user_" + random.nextInt(1000);
    }

    /**
     * Получает общее количество мемов
     */
    public int getTotalMemes() {
        return (int) memeRepository.count();
    }

    /**
     * Получает количество мемов, созданных сегодня
     */
    public int getTodayMemes() {
        LocalDateTime startOfDay = getStartOfDay();
        return memeRepository.findByCreatedAtAfter(startOfDay).size();
    }

    /**
     * Проверяет, достиг ли пользователь дневного лимита генерации ИИ-мемов
     */
    public boolean hasReachedAiLimit(User user) {
        Objects.requireNonNull(user, "User cannot be null");

        if (user.getIsPremium()) {
            return false; // Premium users have no limits
        }

        LocalDateTime startOfDay = getStartOfDay();
        List<Meme> todayMemes = memeRepository.findByUserIdAndTypeAndCreatedAtAfter(user.getTelegramId(),
                Meme.MemeType.AI_GENERATED, startOfDay);

        return !todayMemes.isEmpty();
    }

    /**
     * Возвращает начало текущего дня
     */
    private LocalDateTime getStartOfDay() {
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Выдает премиум-статус пользователю по ID
     *
     * @param userId ID пользователя
     */
    @Transactional
    public void awardPremiumStatus(String userId) {
        if (!StringUtils.hasText(userId)) {
            log.warn("{}Cannot award premium status: user ID is empty", LOG_PREFIX);
            return;
        }

        userRepository.findByTelegramId(userId).ifPresentOrElse(this::awardPremiumStatus, () -> log.warn("{}User not "
                + "found: {}", LOG_PREFIX, userId));
    }

    /**
     * Выдает премиум-статус пользователю
     */
    private void awardPremiumStatus(User user) {
        if (user.getIsPremium()) {
            log.info("{}User {} already has premium status", LOG_PREFIX, user.getTelegramId());
            return;
        }

        user.setIsPremium(true);
        user.setPremiumSince(LocalDateTime.now());
        userRepository.save(user);
        log.info("{}Awarded premium status to user: {}", LOG_PREFIX, user.getTelegramId());
    }

    /**
     * Генерирует случайный ID
     */
    private String generateRandomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * Submit meme to contest
     *
     * @param memeUrl URL of the meme to submit
     * @param userId  ID of the user submitting the meme
     * @return true if submission was successful, false otherwise
     */
    @Transactional
    public boolean submitMemeToContest(String memeUrl, String userId) {
        validateParams(memeUrl, userId, "Invalid parameters for contest submission");
        return contestService.submitMemeToContest(memeUrl, userId);
    }

    /**
     * Vote for a meme
     *
     * @param memeId ID of the meme to vote for
     * @return true if vote was successful, false otherwise
     */
    @Transactional
    public boolean voteMeme(Long memeId) {
        if (memeId == null) {
            log.warn("{}Cannot vote for meme: meme ID is null", LOG_PREFIX);
            return false;
        }

        return memeRepository.findById(memeId).map(meme -> {
            meme.setLikes(meme.getLikes() + 1);
            memeRepository.save(meme);
            log.info("{}Vote added to meme: {}", LOG_PREFIX, memeId);
            return true;
        }).orElseGet(() -> {
            log.warn("{}Meme not found for voting: {}", LOG_PREFIX, memeId);
            return false;
        });
    }

    /**
     * Получить топ мемов по лайкам
     *
     * @return List of top memes
     */
    public List<Meme> getTopMemes() {
        log.debug("{}Fetching top memes", LOG_PREFIX);
        return memeRepository.findTop10ByOrderByLikesDesc();
    }

    /**
     * Получить мемы участвующие в конкурсе
     *
     * @return List of memes in contest
     */
    public List<Meme> getContestMemes() {
        log.debug("{}Fetching contest memes", LOG_PREFIX);
        return memeRepository.findByInContestIsTrue();
    }

    public List<Meme> getMemesByUser(String userId) {
        log.debug("{}Fetching user memes", LOG_PREFIX);
        return memeRepository.findByUserId(userId);
    }

    /**
     * Исключение для ошибок генерации мемов
     */
    public static class MemeGenerationException extends RuntimeException {
        public MemeGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
