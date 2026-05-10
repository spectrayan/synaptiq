# ── Synaptiq API — Production Dockerfile ─────────────────────────────────────
# Multi-stage build: compile + slim runtime image
# Target: Cloud Run (port 8080)
# ─────────────────────────────────────────────────────────────────────────────

# Stage 1 — Build with Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and POM first (layer caching for dependencies)
COPY pom.xml ./
# Download dependencies (cached unless pom.xml changes)
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B -q

# Copy source and build
COPY src/ ./src/
RUN mvn clean package -DskipTests -B -q

# Stage 2 — Runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S synaptiq && adduser -S synaptiq -G synaptiq

# Copy JAR from builder
COPY --from=builder /build/target/spring-apis-*.jar app.jar

# Set ownership
RUN chown -R synaptiq:synaptiq /app

USER synaptiq

EXPOSE 8080

# Health check for Cloud Run / Docker Compose
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
