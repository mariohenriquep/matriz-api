# syntax=docker/dockerfile:1
# Imagem do backend da Matriz (Ficha 05, Ex. 2). Construir a partir da pasta backend/:
#   docker build -t matriz-backend backend
# A configuração (base de dados, segredo JWT, CORS) chega por variáveis de ambiente no arranque;
# nenhum valor sensível fica na imagem.

# 1) Build: compila e empacota o JAR com o Maven Wrapper do projeto
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# As dependências ficam numa camada própria: só se descarregam de novo quando o pom.xml muda
RUN ./mvnw -B -q dependency:go-offline
COPY src/ src/
# Os testes correm antes, na CI (./mvnw verify); aqui só se empacota
RUN ./mvnw -B -q package -DskipTests && cp target/matriz-backend-*.jar app.jar

# 2) Execução: só o JRE e o JAR, com um utilizador sem privilégios
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S matriz && adduser -S -G matriz matriz
WORKDIR /app
COPY --from=build /app/app.jar app.jar
USER matriz
ENV MATRIZ_PORTA=8080
EXPOSE 8080
# Saudável quando a API responde e a base de dados está ligada ({"status":"UP"})
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
  CMD wget -qO- "http://localhost:${MATRIZ_PORTA}/actuator/health" | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
