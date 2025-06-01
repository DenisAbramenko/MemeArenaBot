package org.abr.memearenabot.service;

import jakarta.annotation.PostConstruct;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Сервис для работы с мемами
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemeService {
    private static final String LOG_PREFIX = "[Meme-Service] ";
    private static final int FREE_USER_AI_MEME_DAILY_LIMIT = 1;
    private static final int FREE_USER_TEMPLATE_MEME_DAILY_LIMIT = 1;
    private static final List<String> DEFAULT_TEMPLATES = Arrays.asList("drake", "distracted_boyfriend", "two_buttons"
            , "change_my_mind", "expanding_brain");

    private final Random random = new Random();
    private final List<String> memeTemplates = new CopyOnWriteArrayList<>();
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
    @Getter
    @Setter
    private boolean voiceEnabled = true;

    @Autowired
    public void setContestService(ContestService contestService) {
        this.contestService = contestService;
    }

    /**
     * Инициализация стандартных шаблонов мемов
     */
    @PostConstruct
    public void init() {
        memeTemplates.addAll(DEFAULT_TEMPLATES);
        log.info("{}Initialized with {} templates", LOG_PREFIX, memeTemplates.size());
    }

    /**
     * Генерирует мем с помощью ИИ на основе текстового описания
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

            // Сохранение в базу данных
            Meme meme = createMeme(localImageUrl, description, user, Meme.MemeType.AI_GENERATED);

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
            memeRepository.save(meme);

            log.debug("{}Generated AI meme with ID: {}", LOG_PREFIX, meme.getId());
            return localImageUrl;
        } catch (Exception e) {
            log.error("{}Error generating AI meme: {}", LOG_PREFIX, e.getMessage(), e);
            throw new MemeGenerationException("Failed to generate AI meme", e);
        }
    }

    /**
     * Генерирует мем используя шаблон
     *
     * @param templateId ID шаблона
     * @param textLines  Текстовые строки для шаблона
     * @param user       Пользователь
     * @return URL сгенерированного мема
     * @throws IllegalArgumentException если входные параметры некорректны
     * @throws MemeGenerationException  если возникла ошибка при генерации
     */
    @Async
    @Transactional
    public CompletableFuture<String> generateMemeFromTemplate(String templateId, List<String> textLines, User user) {
        validateTemplateParams(templateId, textLines);
        Objects.requireNonNull(user, "User cannot be null");

        if (hasReachedTemplateLimit(user)) {
            log.warn("{}User {} has reached daily template limit", LOG_PREFIX, user.getTelegramId());
            return CompletableFuture.failedFuture(new MemeGenerationException("Daily template usage limit reached",
                    null));
        }

        try {
            log.info("{}Generating template meme for user {}: {}", LOG_PREFIX, user.getTelegramId(), templateId);

            String imageUrl = generateTemplateMemeUrl(templateId);

            // Сохранение в базу данных
            Meme meme = createMeme(imageUrl, templateId, user, Meme.MemeType.TEMPLATE_BASED);

            log.debug("{}Generated template meme with ID: {}", LOG_PREFIX, meme.getId());
            return CompletableFuture.completedFuture(imageUrl);
        } catch (IllegalArgumentException e) {
            log.warn("{}Invalid input for template meme: {}", LOG_PREFIX, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("{}Error generating template meme: {}", LOG_PREFIX, e.getMessage(), e);
            throw new MemeGenerationException("Failed to generate template meme", e);
        }
    }

    /**
     * Генерирует URL для шаблонного мема
     */
    private String generateTemplateMemeUrl(String templateId) {
        // TODO: Integrate with template-based meme generator
        // For now, return a mock URL
        String memeId = generateRandomId();
        return memeStorageUrl + "template_" + templateId + "_" + memeId + ".jpg";
    }

    /**
     * Генерирует мем используя шаблон (упрощенный метод)
     *
     * @param templateId ID шаблона
     * @param textLines  Текстовые строки для шаблона
     * @return URL сгенерированного мема
     * @throws IllegalArgumentException если входные параметры некорректны
     * @throws MemeGenerationException  если возникла ошибка при генерации
     */
    @Transactional
    public String generateMemeFromTemplate(String templateId, List<String> textLines) {
        validateTemplateParams(templateId, textLines);

        try {
            log.info("{}Generating template meme: {}", LOG_PREFIX, templateId);

            String imageUrl = generateTemplateMemeUrl(templateId);

            // Создание временного ID пользователя
            String tempUserId = generateTempUserId();

            // Сохранение в базу данных
            Meme meme = new Meme(imageUrl, templateId, tempUserId, true);
            meme.setType(Meme.MemeType.TEMPLATE_BASED);
            memeRepository.save(meme);

            log.debug("{}Generated template meme with ID: {}", LOG_PREFIX, meme.getId());
            return imageUrl;
        } catch (IllegalArgumentException e) {
            log.warn("{}Invalid input for template meme: {}", LOG_PREFIX, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("{}Error generating template meme: {}", LOG_PREFIX, e.getMessage(), e);
            throw new MemeGenerationException("Failed to generate template meme", e);
        }
    }

    /**
     * Генерирует мем из голосового сообщения
     *
     * @param voiceData Байты голосового сообщения
     * @param user      Пользователь
     * @return URL сгенерированного мема
     * @throws IllegalArgumentException если входные параметры некорректны
     * @throws MemeGenerationException  если возникла ошибка при генерации
     */
    @Async
    @Transactional
    public CompletableFuture<String> generateMemeFromVoice(byte[] voiceData, User user) {
        validateVoiceData(voiceData);
        Objects.requireNonNull(user, "User cannot be null");

        if (!voiceEnabled) {
            log.warn("{}Voice meme generation is disabled", LOG_PREFIX);
            return CompletableFuture.failedFuture(new MemeGenerationException("Voice meme generation is currently " +
                    "disabled", null));
        }

        try {
            log.info("{}Generating voice meme for user {}, data size: {} bytes", LOG_PREFIX, user.getTelegramId(),
                    voiceData.length);

            String imageUrl = generateVoiceMemeUrl();

            // Сохранение в базу данных
            Meme meme = createMeme(imageUrl, "Voice meme", user, Meme.MemeType.VOICE_GENERATED);

            log.debug("{}Generated voice meme with ID: {}", LOG_PREFIX, meme.getId());
            return CompletableFuture.completedFuture(imageUrl);
        } catch (IllegalArgumentException e) {
            log.warn("{}Invalid input for voice meme: {}", LOG_PREFIX, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("{}Error generating voice meme: {}", LOG_PREFIX, e.getMessage(), e);
            throw new MemeGenerationException("Failed to generate voice meme", e);
        }
    }

    /**
     * Генерирует URL для голосового мема
     */
    private String generateVoiceMemeUrl() {
        // TODO: Integrate with speech-to-text service, then process with AI
        // For now, return a mock URL
        String memeId = generateRandomId();
        return memeStorageUrl + "voice_generated_" + memeId + ".jpg";
    }

    /**
     * Генерирует мем из голосового сообщения (упрощенный метод)
     *
     * @param voiceData Байты голосового сообщения
     * @return URL сгенерированного мема
     * @throws IllegalArgumentException если входные параметры некорректны
     * @throws MemeGenerationException  если возникла ошибка при генерации
     */
    @Transactional
    public String generateMemeFromVoice(byte[] voiceData) {
        validateVoiceData(voiceData);

        if (!voiceEnabled) {
            log.warn("{}Voice meme generation is disabled", LOG_PREFIX);
            throw new MemeGenerationException("Voice meme generation is currently disabled", null);
        }

        try {
            log.info("{}Generating voice meme, data size: {} bytes", LOG_PREFIX, voiceData.length);

            String imageUrl = generateVoiceMemeUrl();

            // Создание временного ID пользователя
            String tempUserId = generateTempUserId();

            // Сохранение в базу данных
            Meme meme = new Meme();
            meme.setImageUrl(imageUrl);
            meme.setUserId(tempUserId);
            meme.setType(Meme.MemeType.VOICE_GENERATED);
            memeRepository.save(meme);

            log.debug("{}Generated voice meme with ID: {}", LOG_PREFIX, meme.getId());
            return imageUrl;
        } catch (IllegalArgumentException e) {
            log.warn("{}Invalid input for voice meme: {}", LOG_PREFIX, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("{}Error generating voice meme: {}", LOG_PREFIX, e.getMessage(), e);
            throw new MemeGenerationException("Failed to generate voice meme", e);
        }
    }

    /**
     * Получает доступные шаблоны мемов
     *
     * @return Список доступных шаблонов
     */
    @Cacheable(value = "memeTemplates")
    public List<String> getAvailableTemplates() {
        log.debug("{}Fetching available meme templates", LOG_PREFIX);
        return Collections.unmodifiableList(new ArrayList<>(memeTemplates));
    }

    /**
     * Добавляет новый шаблон
     *
     * @param templateName Название шаблона
     * @throws IllegalArgumentException если название пустое
     */
    @CacheEvict(value = "memeTemplates", allEntries = true)
    public void addTemplate(String templateName) {
        if (!StringUtils.hasText(templateName)) {
            throw new IllegalArgumentException("Template name cannot be empty");
        }

        if (!memeTemplates.contains(templateName)) {
            memeTemplates.add(templateName);
            log.info("{}Added new template: {}", LOG_PREFIX, templateName);
        } else {
            log.debug("{}Template already exists: {}", LOG_PREFIX, templateName);
        }
    }

    /**
     * Удаляет шаблон
     *
     * @param templateName Название шаблона
     * @return true если шаблон был удален
     * @throws IllegalArgumentException если название пустое
     */
    @CacheEvict(value = "memeTemplates", allEntries = true)
    public boolean removeTemplate(String templateName) {
        if (!StringUtils.hasText(templateName)) {
            throw new IllegalArgumentException("Template name cannot be empty");
        }

        boolean removed = memeTemplates.remove(templateName);
        if (removed) {
            log.info("{}Removed template: {}", LOG_PREFIX, templateName);
        } else {
            log.debug("{}Template not found: {}", LOG_PREFIX, templateName);
        }

        return removed;
    }

    /**
     * Публикует мем в ленту
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
     * Создает NFT для мема
     *
     * @param memeUrl URL мема
     * @param userId  ID пользователя
     * @return URL созданного NFT или null в случае ошибки
     */
    @Transactional
    public String createNFT(String memeUrl, String userId) {
        validateParams(memeUrl, userId, "Invalid parameters for NFT creation");

        return findMemeByUrl(memeUrl).map(meme -> createNftForMeme(meme, userId)).orElseGet(() -> {
            log.warn("{}Meme not found for URL: {}", LOG_PREFIX, memeUrl);
            return null;
        });
    }

    /**
     * Создает NFT для мема
     */
    private String createNftForMeme(Meme meme, String userId) {
        // Проверка, что NFT еще не создан
        if (meme.getNftUrl() != null) {
            log.warn("{}NFT already exists for meme {}", LOG_PREFIX, meme.getId());
            return meme.getNftUrl();
        }

        // Здесь должна быть интеграция с NFT маркетплейсом
        String nftUrl = "https://nft-marketplace.com/token/" + generateRandomId();
        meme.setNftUrl(nftUrl);
        meme.setNftCreatedAt(LocalDateTime.now());
        memeRepository.save(meme);

        log.info("{}NFT created for meme {} by user {}: {}", LOG_PREFIX, meme.getId(), userId, nftUrl);
        return nftUrl;
    }

    /**
     * Создает мем и добавляет его пользователю
     */
    @Transactional
    protected Meme createMeme(String imageUrl, String description, User user, Meme.MemeType type) {
        Meme meme = new Meme(imageUrl, description, user);
        meme.setType(type);
        memeRepository.save(meme);

        // Добавление мема в коллекцию пользователя
        user.addMeme(meme);
        userRepository.save(user);

        return meme;
    }

    /**
     * Проверяет параметры шаблона
     */
    private void validateTemplateParams(String templateId, List<String> textLines) {
        if (!StringUtils.hasText(templateId)) {
            throw new IllegalArgumentException("Template ID cannot be empty");
        }

        if (textLines == null || textLines.isEmpty()) {
            throw new IllegalArgumentException("Text lines cannot be empty");
        }
    }

    /**
     * Проверяет данные голосового сообщения
     */
    private void validateVoiceData(byte[] voiceData) {
        if (voiceData == null || voiceData.length == 0) {
            throw new IllegalArgumentException("Voice data cannot be empty");
        }
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
     * Проверяет, достиг ли пользователь дневного лимита использования шаблонов
     */
    public boolean hasReachedTemplateLimit(User user) {
        Objects.requireNonNull(user, "User cannot be null");

        if (user.getIsPremium()) {
            return false; // Premium users have no limits
        }

        LocalDateTime startOfDay = getStartOfDay();
        List<Meme> todayMemes = memeRepository.findByUserIdAndTypeAndCreatedAtAfter(user.getTelegramId(),
                Meme.MemeType.TEMPLATE_BASED, startOfDay);

        return !todayMemes.isEmpty();
    }

    /**
     * Возвращает начало текущего дня
     */
    private LocalDateTime getStartOfDay() {
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Выдает премиум-статус пользователю
     *
     * @param userId ID пользователя
     */
    @Transactional
    public void awardPremiumStatus(String userId) {
        if (!StringUtils.hasText(userId)) {
            log.warn("{}Cannot award premium status: user ID is empty", LOG_PREFIX);
            return;
        }

        userRepository.findByTelegramId(userId).ifPresentOrElse(this::awardPremiumStatus, () -> log.warn("{}User not " +
                "found: {}", LOG_PREFIX, userId));
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
     * Get top memes sorted by likes
     * 
     * @return List of top memes
     */
    public List<Meme> getTopMemes() {
        log.debug("{}Fetching top memes", LOG_PREFIX);
        return memeRepository.findTop10ByOrderByLikesDesc();
    }
    
    /**
     * Get memes currently in contest
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
