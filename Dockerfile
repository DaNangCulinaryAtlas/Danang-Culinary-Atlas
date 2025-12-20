# FROM openjdk:21-jdk-slim
# FROM public.ecr.aws/docker/library/openjdk:21-jdk-slim
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]