FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/cloud-gateway-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
