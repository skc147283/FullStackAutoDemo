FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY api/pom.xml ./api/pom.xml
COPY ui-tests/pom.xml ./ui-tests/pom.xml
COPY db-tests/pom.xml ./db-tests/pom.xml
COPY api/src ./api/src
RUN mvn -B -pl api -am -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/api/target/wealth-api-0.0.1-SNAPSHOT-exec.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
