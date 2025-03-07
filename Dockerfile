# Use an official OpenJDK runtime as a parent image
FROM arm64v8/openjdk:21-jdk



# Set the MAVEN_VERSION environment variable
ENV MAVEN_VERSION=3.8.6

# Install Maven
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set the MAVEN_HOME environment variable
ENV MAVEN_HOME=/opt/maven



# Set the working directory in the container
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -X


# COPY - from=build /app/target/my-application.jar .
COPY /app/target/dbspringload-0.0.1-SNAPSHOT.jar .

# Expose the port that the application will run on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/dbspringload-0.0.1-SNAPSHOT.jar"]

