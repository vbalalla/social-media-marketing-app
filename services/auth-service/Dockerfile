# =============================================================================
# Multi-stage Dockerfile — Auth Service (Spring Boot 3 / Java 21)
# =============================================================================

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache dependencies layer — only invalidated when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build the application
COPY src ./src
RUN mvn package -DskipTests -q

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root user
RUN addgroup -S serendia && adduser -S serendia -G serendia
USER serendia

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# JVM tuning for containers (Java 21 container-aware by default)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
