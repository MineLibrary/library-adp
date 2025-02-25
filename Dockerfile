# Use an official Gradle image to build the project
FROM gradle:8.3-jdk17 AS builder
 
# Set the working directory
WORKDIR /app

# Copy the entire project to the container
COPY . .

# Build the project using Gradle
RUN gradle build --no-daemon

# Use a lightweight OpenJDK image for running the application
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application's port
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]