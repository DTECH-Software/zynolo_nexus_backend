# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Pull dependencies separately for better caching during iterative builds
COPY pom.xml ./
RUN mvn -B dependency:go-offline

# Copy the remaining project files and build the executable jar
COPY src ./src
RUN mvn -B package -DskipTests && \
    set -eux; \
    jar="$(ls target/*.jar | grep -v 'original' | head -n 1)"; \
    cp "$jar" /app/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=default

COPY --from=builder /app/app.jar ./app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
