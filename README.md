# MemeArenaBot

Бот для Telegram, который позволяет генерировать и обмениваться мемами, участвовать в конкурсах мемов и создавать коллекции.

## 🛠️ Технологический стек

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" /></a>
  <a href="#"><img src="https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white" alt="Spring" /></a>
  <a href="#"><img src="https://img.shields.io/badge/Spring_Boot-%236DB33F.svg?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot" /></a>
  <a href="#"><img src="https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" /></a>
  <a href="#"><img src="https://img.shields.io/badge/Liquibase-%23FF9900.svg?style=for-the-badge&logo=liquibase&logoColor=white" alt="Liquibase" /></a>
  <a href="#"><img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" /></a>
  <a href="#"><img src="https://img.shields.io/badge/Telegram-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram" /></a>
  <a href="#"><img src="https://img.shields.io/badge/OpenAI-%23412991.svg?style=for-the-badge&logo=openai&logoColor=white" alt="OpenAI" /></a>
  <a href="#"><img src="https://img.shields.io/badge/Lombok-%23E34F26.svg?style=for-the-badge&logo=lombok&logoColor=white" alt="Lombok" /></a>
  <a href="#"><img src="https://img.shields.io/badge/Maven-%23C71A36.svg?style=for-the-badge&logo=apache-maven&logoColor=white" alt="Maven" /></a>
</p>

## Функции

- Генерация мемов на основе текстового описания с помощью ИИ (DALL-E, Stability AI)
- Создание мемов из шаблонов
- Генерация мемов из голосовых сообщений
- Проведение конкурсов мемов
- Премиум-подписка с расширенными функциями
- Админ-панель для модерации
- NFT для мемов

## Настройка и запуск

### Предварительные требования

- JDK 17 или выше
- PostgreSQL 14
- Maven
- Зарегистрированный Telegram бот (через BotFather)
- API ключи для OpenAI и/или Stability AI

### Установка

1. Клонировать репозиторий:
```bash
git clone https://github.com/yourusername/memearenabot.git
cd memearenabot
```

2. Настроить конфигурацию:
   - Скопировать `application-example.properties` в `application.properties`
   - Заполнить необходимые параметры (токен бота, ключи API и т.д.)

3. Собрать проект:
```bash
mvn clean package
```

4. Запустить приложение:
```bash
java -jar target/memearenabot-0.0.1-SNAPSHOT.jar
```

## Запуск с использованием Docker

### Требования
- Docker
- Docker Compose

### Настройка

1. Создайте файл `.env` в корне проекта на основе следующего шаблона:
```
# API ключи для генерации изображений
OPENAI_API_KEY=your_openai_api_key_here
STABILITY_API_KEY=your_stability_api_key_here

# Настройки Telegram бота
TELEGRAM_BOT_TOKEN=your_telegram_bot_token_here
TELEGRAM_BOT_USERNAME=your_bot_username_here
```

2. Запустите приложение с помощью Docker Compose:
```bash
docker-compose up -d
```

3. Для остановки контейнеров:
```bash
docker-compose down
```

4. Для просмотра логов:
```bash
docker-compose logs -f app
```

### Структура Docker-окружения

- **app** - контейнер с Java-приложением
- **db** - контейнер с PostgreSQL
- **volumes** - постоянные хранилища данных:
  - postgres-data: данные PostgreSQL
  - meme-storage: хранилище изображений мемов
  - app-logs: логи приложения

### Настройка ИИ-интеграции

Для работы с генерацией изображений необходимо получить API ключи:

1. OpenAI DALL-E:
   - Зарегистрируйтесь на [OpenAI Platform](https://platform.openai.com/)
   - Создайте API ключ
   - Добавьте ключ в `application.properties` (параметр `ai.openai.api-key`)

2. Stability AI:
   - Зарегистрируйтесь на [Stability AI](https://stability.ai/)
   - Создайте API ключ
   - Добавьте ключ в `application.properties` (параметр `ai.stability.api-key`)

Бот попытается использовать сначала DALL-E, затем Stability AI. Можно настроить только один из сервисов.

## Структура конкурсов мемов

1. Создание конкурса администратором (тема, длительность, призы)
2. Подача мемов участниками
3. Голосование за мемы
4. Определение победителей и награждение

## Премиум-функции

- Неограниченное количество генераций мемов с помощью ИИ
- Доступ к эксклюзивным шаблонам
- Возможность создания NFT для мемов
- Приоритетное участие в конкурсах
- Расширенная статистика

## Вклад в проект

Если вы хотите внести свой вклад в проект, пожалуйста:
1. Создайте форк репозитория
2. Создайте ветку для вашей функциональности
3. Создайте Pull Request с описанием изменений

## Лицензия

[MIT](LICENSE) 

## Архитектура проекта

### Основные компоненты

```
src/
├── main/
│   ├── java/
│   │   └── org/abr/memearenabot/
│   │       ├── bot/            # Telegram бот и обработчики команд
│   │       ├── config/         # Конфигурация приложения
│   │       ├── controller/     # REST контроллеры
│   │       ├── dto/            # Data Transfer Objects
│   │       ├── exception/      # Обработка исключений
│   │       ├── model/          # Модели данных
│   │       ├── repository/     # Репозитории для работы с БД
│   │       ├── service/        # Бизнес-логика
│   │       │   ├── ai/         # Сервисы для работы с ИИ
│   │       │   └── contest/    # Сервисы для работы с конкурсами
│   │       └── MemeArenaBotApplication.java
│   └── resources/
│       ├── db/
│       │   └── changelog/      # Миграции Liquibase
│       ├── static/             # Статические файлы
│       ├── templates/          # Шаблоны
│       └── application.properties
└── test/                       # Тесты
```

### Структура базы данных

- **users** - информация о пользователях
- **memes** - хранение мемов
- **contests** - конкурсы мемов
- **contest_entries** - заявки на участие в конкурсах
- **contest_votes** - голоса за мемы в конкурсах 