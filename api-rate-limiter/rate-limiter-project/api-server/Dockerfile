# Stage 1: Build
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
# Copy Gradle files
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .

# Copy our custom modules
COPY rate-limiter-core rate-limiter-core
COPY api-server api-server
COPY proxy-server proxy-server
# Build the JAR
RUN ./gradlew :api-server:bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/api-server/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
