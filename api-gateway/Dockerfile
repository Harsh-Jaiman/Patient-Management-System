FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build jar
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk AS runner
WORKDIR /app

# Copy the jar built in previous stage
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 4004

# Command to run the application
ENTRYPOINT ["java","-jar","app.jar"]
