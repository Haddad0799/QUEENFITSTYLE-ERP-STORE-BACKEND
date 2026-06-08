# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -pl app -am

# ---- runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/app/target/app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
