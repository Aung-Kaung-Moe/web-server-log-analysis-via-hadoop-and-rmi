# Runtime only (no build stage)
# Use Microsoft registry to avoid DockerHub TLS issues
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

WORKDIR /app
RUN mkdir -p /app/logs

# Copy the jar built on your Windows machine
COPY target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
