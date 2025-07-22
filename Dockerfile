FROM openjdk:17-jdk-slim

WORKDIR /app

# JAR dosyasını kopyala
COPY target/KafkaApp-1.0-SNAPSHOT.jar app.jar

# Port aç (gerekirse)
EXPOSE 8080

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"] 