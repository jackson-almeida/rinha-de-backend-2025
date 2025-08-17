package com.rinha_de_backend_2025.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinha_de_backend_2025.models.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class HealthStatusCache {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY = "service-health:last-check";
    private static final Duration TTL = Duration.ofSeconds(5);

    public Optional<HealthStatus> getCachedStatus() {
        String json = redisTemplate.opsForValue().get(KEY);
        if (json == null) return Optional.empty();
        try {
            ObjectMapper mapper = new ObjectMapper();
            return Optional.of(mapper.readValue(json, HealthStatus.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void cacheStatus(HealthStatus status) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(status);
            redisTemplate.opsForValue().set(KEY, json, TTL);
        } catch (Exception e) {
            // log
        }
    }
}
