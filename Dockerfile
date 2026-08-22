# syntax=docker/dockerfile:1
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline
COPY src src
RUN mvn -B -DskipTests package && cp "$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n1)" /workspace/app.jar

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /workspace/app.jar /app/app.jar
USER 10001
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=staging
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-Djava.security.egd=file:/dev/urandom","-jar","/app/app.jar"]
