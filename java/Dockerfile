# syntax=docker/dockerfile:1

# ---- Build: full JDK, compiles and packages the app ----
FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

# The spec lives at the repo root; build.gradle.kts reads it from ../greetings.yaml
COPY greetings.yaml ./greetings.yaml
COPY java ./java

WORKDIR /workspace/java
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x ./gradlew \
 && ./gradlew bootJar --no-daemon --console=plain

# ---- Runtime: JRE only ----
FROM eclipse-temurin:25-jre

RUN groupadd --system app \
 && useradd --system --gid app --no-create-home app

WORKDIR /app
COPY --from=build --chown=app:app /workspace/java/build/libs/greetings.jar app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]