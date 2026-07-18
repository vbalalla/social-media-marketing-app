# =============================================================================
# Multi-stage Dockerfile — AI Service (Spring Boot 3 / Java 21)
# =============================================================================

FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S serendia && adduser -S serendia -G serendia
USER serendia
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# AI service may need more heap for LLM response buffering
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8083
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
