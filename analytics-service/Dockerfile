FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-avro ./common-avro
COPY api-gateway ./api-gateway
COPY order-service ./order-service
COPY analytics-service ./analytics-service

RUN mvn clean package -pl analytics-service -am -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/analytics-service/target/analytics-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
