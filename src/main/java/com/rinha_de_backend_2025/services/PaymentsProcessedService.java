package com.rinha_de_backend_2025.services;

import com.rinha_de_backend_2025.models.PaymentWireRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentsProcessedService {
    private static final String PAYMENTS_PROCESSED = "payments_processed";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void addPaymentProcessed(PaymentWireRequest request) {
        redisTemplate.opsForList().rightPush(PAYMENTS_PROCESSED, request);

        // Remover posteriormente
        List<Object> fila = redisTemplate.opsForList().range(PAYMENTS_PROCESSED, 0, -1);
        if (fila != null) {
            fila.forEach((item) -> System.out.println("Base de pagamentos processados: " + item));
        }
        // =====
    }

    public PaymentWireRequest blockingDequeue() {
        Object result = redisTemplate.opsForList().leftPop(PAYMENTS_PROCESSED, 30, TimeUnit.MICROSECONDS);
        return (PaymentWireRequest) result;
    }

    public long getQueueSize() {
        return redisTemplate.opsForList().size(PAYMENTS_PROCESSED);
    }
}
