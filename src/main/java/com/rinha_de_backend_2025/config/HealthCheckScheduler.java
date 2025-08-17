package com.rinha_de_backend_2025.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HealthCheckScheduler {

    private final RestTemplate restTemplate;
    private final boolean isMaster;
    private final String healthCheckUrl;

    public HealthCheckScheduler(RestTemplate restTemplate,
                                @Value("${backend.master}") boolean isMaster,
                                @Value("${healthcheck.url}") String healthCheckUrl) {
        this.restTemplate = restTemplate;
        this.isMaster = isMaster;
        this.healthCheckUrl = healthCheckUrl;
    }

    @Scheduled(fixedRate = 5000)
    public void checkServiceHealth() {
        if (!isMaster) {
            return;
        }

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(healthCheckUrl, String.class);
            System.out.println("Health check response: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Erro no health check: " + e.getMessage());
        }
    }
}


