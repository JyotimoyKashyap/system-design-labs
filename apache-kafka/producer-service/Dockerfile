FROM gradle:jdk26 AS build
WORKDIR /workspace/app

COPY build.gradle settings.gradle ./
COPY src src

RUN gradle build -x test

FROM cgr.dev/chainguard/jre:latest
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
