FROM alpine/java:21-jre

LABEL org.opencontainers.image.source=https://github.com/MarinaPimenova/ti-ai-orchestrator-api

COPY build/libs/*.jar /app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app.jar"]
