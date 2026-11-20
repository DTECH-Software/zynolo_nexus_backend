# syntax=docker/dockerfile:1
# Build from repo root: docker compose -f fleet_service/docker-compose.yml build
# Or: docker build -f fleet_service/Dockerfile .

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY zynolo-nexus-contracts /build/zynolo-nexus-contracts
RUN mvn -f /build/zynolo-nexus-contracts/pom.xml -B install -DskipTests

WORKDIR /build/fleet_service
COPY fleet_service/pom.xml ./
RUN mvn -B dependency:go-offline

COPY fleet_service/src ./src
RUN mvn -B package -DskipTests && \
    set -eux; \
    jar="$(ls target/*.jar | grep -v 'original' | head -n 1)"; \
    cp "$jar" /build/fleet_service/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=default

COPY --from=builder /build/fleet_service/app.jar ./app.jar
EXPOSE 8096

ENTRYPOINT ["java", "-jar", "/app/app.jar"]