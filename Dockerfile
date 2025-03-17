FROM openjdk:23-slim AS builder

RUN echo "Dockerfile version 2025-03-17"

WORKDIR /app

COPY . .

RUN ./mvnw clean install

RUN ls -al

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/target/dbspringload-0.0.1-SNAPSHOT.jar"]
