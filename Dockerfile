FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copia el proyecto y construye el artefacto Spring Boot dentro del contenedor
COPY . .
RUN chmod +x mvnw && ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/target/ai-proxy-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/app.jar"]
