FROM maven:3.9-amazoncorretto-17 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven configuration files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create the runtime image
FROM amazoncorretto:17-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Create directory for file uploads
RUN mkdir -p /app/uploads && \
    chmod 777 /app/uploads

# Expose the port the app runs on
EXPOSE 8080
EXPOSE 9092

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV UPLOAD_DIR=/app/uploads

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 