FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/auth-server-4.0.0.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java","-jar","app.jar"]
