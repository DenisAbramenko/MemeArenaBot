FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Копируем POM и загружаем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src/ /app/src/
RUN mvn package -DskipTests -B

# Финальный образ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаем пользователя без привилегий
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Создаем необходимые директории
RUN mkdir -p /app/logs /app/meme-storage
RUN chown -R appuser:appgroup /app

# Копируем JAR из стадии сборки
COPY --from=build /app/target/*.jar app.jar

# Переключаемся на пользователя без привилегий
USER appuser

# Настройки среды выполнения
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENV SPRING_PROFILES_ACTIVE="prod"

# Экспонируем порт и точка входа
EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -jar /app/app.jar 