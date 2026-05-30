# Stage 1: Build the application
FROM gradle:jdk26 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# Stage 2: Run the application
FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]