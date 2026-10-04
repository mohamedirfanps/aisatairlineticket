# ---- Build stage: compile and package the Spring Boot fat jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies separately from source for faster rebuilds.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Runtime stage: slim JRE with just the jar ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user.
RUN useradd -r -u 1001 appuser
COPY --from=build /app/target/airline-booking-system-1.0-SNAPSHOT.jar app.jar
RUN mkdir -p /app/data && chown -R appuser /app
USER appuser

# Ports are supplied by the platform via $PORT; 8080 is the local default.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
