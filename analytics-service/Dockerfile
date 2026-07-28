# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/
COPY shipment-service/pom.xml shipment-service/
COPY tracking-service/pom.xml tracking-service/
COPY notification-service/pom.xml notification-service/
COPY analytics-service/pom.xml analytics-service/

RUN mvn dependency:go-offline -pl common,analytics-service -am -q || true

COPY common/src common/src
COPY analytics-service/src analytics-service/src

RUN mvn clean package -pl common,analytics-service -am -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/analytics-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
