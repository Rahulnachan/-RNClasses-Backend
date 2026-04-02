# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
ARG PORT=8085
ENV PORT=${PORT}
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8085
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
