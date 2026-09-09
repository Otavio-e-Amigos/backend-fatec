# Utiliza Java 21 para executar o backend
FROM eclipse-temurin:21-jre

# Define a pasta de trabalho dentro do container
WORKDIR /app

# Copia o JAR gerado pelo Maven para dentro do container
COPY target/backend-fatec-0.0.1-SNAPSHOT.jar app.jar

# Informa que o backend utiliza a porta 8080
EXPOSE 8080

# Inicia o Spring Boot quando o container for executado
ENTRYPOINT ["java", "-jar", "app.jar"]