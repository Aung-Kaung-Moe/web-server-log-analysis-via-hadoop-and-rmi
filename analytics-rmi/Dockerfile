FROM bde2020/hadoop-base:2.0.0-hadoop3.2.1-java8

WORKDIR /app
COPY target/*.jar /app/app.jar

# IMPORTANT: run the main class explicitly
CMD ["java", "-cp", "/app/app.jar", "com.example.analyticsrmi.RmiServerMain"]
