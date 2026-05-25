# Build: docker build -t gerenciador-backend ./backend
# Requer JAR pré-compilado: mvn package antes, ou use o profile local abaixo

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/impressora-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
