# Stage 1: Build the common module + this service
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy root POM and all module POMs for dependency caching
COPY pom.xml .
COPY common/pom.xml common/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/
COPY shipment-service/pom.xml shipment-service/
COPY tracking-service/pom.xml tracking-service/
COPY notification-service/pom.xml notification-service/
COPY analytics-service/pom.xml analytics-service/

# Download dependencies (cached if POMs don't change)
RUN mvn dependency:go-offline -pl common,api-gateway -am -q || true

# Copy source
COPY common/src common/src
COPY api-gateway/src api-gateway/src

# Build common + api-gateway
RUN mvn clean package -pl common,api-gateway -am -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
