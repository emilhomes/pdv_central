# Estágio 1: Construir o projeto (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copia o ficheiro de configuração e o código fonte
COPY pom.xml .
COPY src ./src
# Compila o projeto ignorando os testes
RUN mvn clean package -DskipTests

# Estágio 2: Executar o projeto (Run)
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copia apenas o ficheiro .jar gerado no passo anterior
COPY --from=build /app/target/*.jar app.jar
# Expõe a porta 8080 para a internet
EXPOSE 8080
# Comando para iniciar o Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]