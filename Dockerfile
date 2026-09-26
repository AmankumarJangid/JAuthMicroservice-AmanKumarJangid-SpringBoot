# Single-stage runtime Dockerfile
FROM eclipse-temurin:25-jdk
WORKDIR /app

# Create directory for the certificate
RUN mkdir -p /etc/ssl/certs

# Expecting the jar to already be compiled by GitHub Actions
COPY target/*.jar app.jar

ENV PORT=8080
EXPOSE ${PORT}

# Dynamic entrypoint for your certificate and port
ENTRYPOINT ["sh", "-c", "if [ -n \"$DB_CA_CERT_CONTENT\" ]; then echo \"$DB_CA_CERT_CONTENT\" > /etc/ssl/certs/db-ca.pem; fi && exec java -Dserver.port=${PORT} -jar app.jar"]
