# ↑ билд локально: mvn clean package -DskipTests
# ↓ в контейнере только рантайм

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Копируем готовый JAR, собранный локально в target/
COPY target/cdr-service-*.jar app.jar

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
