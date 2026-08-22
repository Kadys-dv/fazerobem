# syntax=docker/dockerfile:1
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    set -eu; \
    for delay in 0 10 30 60; do \
      if [ "$delay" -gt 0 ]; then sleep "$delay"; fi; \
      if mvn -B -DskipTests -Dmaven.wagon.http.retryHandler.count=3 dependency:go-offline; then \
        exit 0; \
      fi; \
      echo "Maven dependency resolution failed; retrying after backoff..." >&2; \
    done; \
    exit 1
COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    set -eu; \
    for delay in 0 10 30 60; do \
      if [ "$delay" -gt 0 ]; then sleep "$delay"; fi; \
      if mvn -B -DskipTests -Dmaven.wagon.http.retryHandler.count=3 package; then \
        jar="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n1)"; \
        test -n "$jar"; \
        cp "$jar" /workspace/app.jar; \
        exit 0; \
      fi; \
      echo "Maven package failed; retrying after backoff..." >&2; \
    done; \
    exit 1

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /workspace/app.jar /app/app.jar
USER 10001
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=staging
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-Djava.security.egd=file:/dev/urandom","-jar","/app/app.jar"]
