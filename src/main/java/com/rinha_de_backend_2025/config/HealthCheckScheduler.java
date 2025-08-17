package com.rinha_de_backend_2025.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinha_de_backend_2025.models.HealthStatus;
import com.rinha_de_backend_2025.services.HealthStatusCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class HealthCheckScheduler {

    private final RestTemplate restTemplate;
    private final boolean isMaster;
    private final String healthCheckUrl;
    private final HealthStatusCache healthStatusCache;
    private final ObjectMapper objectMapper;

    public HealthCheckScheduler(RestTemplate restTemplate,
                                @Value("${backend.master}") boolean isMaster,
                                @Value("${healthcheck.url}") String healthCheckUrl,
                                HealthStatusCache healthStatusCache) {
        this.restTemplate = restTemplate;
        this.isMaster = isMaster;
        this.healthCheckUrl = healthCheckUrl;
        this.healthStatusCache = healthStatusCache;
        this.objectMapper = new ObjectMapper();
    }

    @Scheduled(fixedRate = 5000)
    public void checkServiceHealth() {
        if (isMaster) {
            performHealthCheck();
        } else {
            checkRedisStatus();
        }
    }

    private void performHealthCheck() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(healthCheckUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                HealthStatus healthStatus = objectMapper.readValue(response.getBody(), HealthStatus.class);
                healthStatusCache.cacheStatus(healthStatus);
                System.out.println("Health check atualizado: " + healthStatus);
            } else {
                // Se não conseguir acessar o health check, assume que está falhando
                HealthStatus failingStatus = new HealthStatus(true, 1000);
                healthStatusCache.cacheStatus(failingStatus);
                System.err.println("Health check falhou - status HTTP: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // Em caso de erro, assume que o serviço está falhando
            HealthStatus failingStatus = new HealthStatus(true, 1000);
            healthStatusCache.cacheStatus(failingStatus);
            System.err.println("Erro no health check: " + e.getMessage());
        }
    }

    private void checkRedisStatus() {
        Optional<HealthStatus> cachedStatus = healthStatusCache.getCachedStatus();
        if (cachedStatus.isPresent()) {
            System.out.println("Status do Redis verificado: " + cachedStatus.get());
        } else {
            System.out.println("Nenhum status encontrado no Redis");
        }
    }
}


