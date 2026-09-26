# Stage 1: Build the application using Maven and Temurin Java 25
FROM maven:3.9.11-eclipse-temurin-25-noble AS build
LABEL authors="AmanKumarJangid"
WORKDIR /app

# Copy the build configuration and source code
COPY pom.xml .
COPY src ./src

# Use native mvn to build
RUN mvn clean package -DskipTests

# Stage 2: Clean Runtime Stage
FROM eclipse-temurin:25-jdk
WORKDIR /app

# Create directory for the certificate in the runtime stage
RUN mkdir -p /etc/ssl/certs

# Copy the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# FIX 1: Provide a fallback port (8080) in case the environment variable isn't set locally
ENV PORT=4134

# Inform Docker that the container will listen on the dynamic port
EXPOSE ${PORT}

# FIX 2: Pass the dynamic port explicitly to Spring Boot using standard flags
ENTRYPOINT ["sh", "-c", "if [ -n \"$DB_CA_CERT_CONTENT\" ]; then echo \"$DB_CA_CERT_CONTENT\" > /etc/ssl/certs/db-ca.pem; fi && exec java -Dserver.port=${PORT} -jar app.jar"]
