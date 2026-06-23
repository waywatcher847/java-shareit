FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY server/target/server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]