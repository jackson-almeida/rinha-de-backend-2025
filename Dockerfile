# Stage 1: Build with GraalVM Native Image (ARM64 or AMD64)
FROM vegardit/graalvm-maven:latest-java24 as builder

WORKDIR /app

# Copy only necessary files to speed up Docker builds
COPY . .

# Build native image using Maven
RUN ./mvnw -Pnative native:compile -DskipTests

# Stage 2: Create minimal runtime container
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y zlib1g && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy binary from previous stage
COPY --from=builder /app/target/rinha-backend-2025-java-spring-graalvm app

# Expose port
EXPOSE 8080

# Run binary
CMD ["./app"]
