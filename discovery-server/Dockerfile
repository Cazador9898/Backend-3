FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/discovery-server-4.0.0.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","app.jar"]
