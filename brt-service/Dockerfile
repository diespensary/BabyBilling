# brt-service/Dockerfile

# Только рантайм — Eclipse Temurin 17 JRE Jammy (multi-arch)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Копируем готовый JAR, собранный локально
COPY target/brt-service-*.jar app.jar

# Запуск
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
