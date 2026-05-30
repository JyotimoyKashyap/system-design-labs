# Stage 1: Build the application using a Gradle image
FROM gradle:jdk26 AS builder
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Build the project, skipping tests to speed up the build
RUN gradle build -x test

# Stage 2: Create the minimal runtime image
FROM cgr.dev/chainguard/jre:latest
WORKDIR /app

# Copy the built JAR from the builder stage into this final image
# (Spring Boot typically suffixes the main jar with SNAPSHOT)
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]