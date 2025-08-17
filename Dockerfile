# Etapa 1: Build nativo com GraalVM oficial + Maven
FROM ghcr.io/graalvm/graalvm-ce:latest AS builder

# Instala Maven e ferramentas básicas
RUN microdnf install -y maven git && microdnf clean all

WORKDIR /app

# Copia metadados Maven primeiro para cache
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Resolve dependências
RUN ./mvnw dependency:resolve

# Copia o restante do projeto
COPY . .

# Compila nativo sem rodar testes
RUN ./mvnw -Pnative native:compile -DskipTests

# Etapa 2: Runtime mínimo baseado em Debian slim
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y zlib1g && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia o binário nativo gerado
COPY --from=builder /app/target/native/backend-native /app/app

# Garante que seja executável
RUN chmod +x /app/app

# Porta padrão
EXPOSE 8080

CMD ["/app/app"]
