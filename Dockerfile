FROM gradle:8.5-jdk17 AS build

ARG VERSION=1.0.0
LABEL version=$VERSION

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew build -x test --no-daemon || true

COPY . .
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar app.jar"]