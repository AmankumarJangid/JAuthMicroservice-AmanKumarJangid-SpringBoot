FROM maven:3.9.11-eclipse-temurin-25-noble AS build
LABEL authors="AmanKumarJangid"
WORKDIR /app


COPY pom.xml .
COPY src ./src


# Compile the application and skip tests for faster deployment
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# Create directory for the certificate
RUN mkdir -p /etc/ssl/certs

# Runtime stage using a clean Temurin JDK 25 image
FROM eclipse-temurin:25-jdk
WORKDIR /app


COPY --from=build /app/target/*.jar app.jar

# Script writes the env variable to a file before launching Java
ENTRYPOINT ["sh", "-c", "echo \"$DB_CA_CERT_CONTENT\" > /etc/ssl/certs/db-ca.pem && java -jar app.jar"]
