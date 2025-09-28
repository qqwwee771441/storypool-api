# Runtime stage
FROM openjdk:21-jdk-slim

WORKDIR /app

# Create non-root user for security
RUN groupadd -r storypool && useradd -r -g storypool storypool

# Copy the built JAR file
COPY /build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown storypool:storypool app.jar

USER storypool

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]