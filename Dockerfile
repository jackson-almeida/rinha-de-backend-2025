# Stage 1: Build nativo
FROM ghcr.io/graalvm/native-image-community:21 AS native-build
WORKDIR /workspace

# Copiar Maven Wrapper e pom.xml
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Baixar dependências
RUN ./mvnw -B -DskipTests dependency:go-offline || true

# Copiar código fonte
COPY src ./src

# Compilar nativo
RUN ./mvnw -B -DskipTests -Pnative native:compile

# Normalizar binário
RUN cp target/native/backend-native /workspace/app

# Stage 2: Runtime minimalista
FROM gcr.io/distroless/cc AS runtime
WORKDIR /app

# Copiar binário do estágio de build
COPY --from=native-build /workspace/app /app/app

EXPOSE 8080
ENTRYPOINT ["/app/app"]
