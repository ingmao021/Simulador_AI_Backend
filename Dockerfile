FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia el JAR generado por Maven
COPY target/ai-proxy-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/app.jar"]

