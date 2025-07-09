# Multi-stage build for security and efficiency
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY src src

RUN chmod +x ./gradlew && ./gradlew build --no-daemon

# Production image
FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=builder /app/build/libs/dash-api-all.jar app.jar

# Create non-root user for security
RUN groupadd -r dashapi && useradd -r -g dashapi dashapi
USER dashapi

EXPOSE 8080
ENV ENVIRONMENT=production

CMD ["java", "-jar", "app.jar"]
