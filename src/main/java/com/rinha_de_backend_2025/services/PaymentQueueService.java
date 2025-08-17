package com.rinha_de_backend_2025.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinha_de_backend_2025.models.PaymentWireRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentQueueService {
    private static final String PAYMENT_QUEUE = "payment_queue";

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void enqueue(PaymentWireRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            redisTemplate.opsForList().rightPush(PAYMENT_QUEUE, json);
        } catch (Exception e) {
            System.err.println("Erro ao enfileirar pagamento: " + e.getMessage());
        }
    }

    public PaymentWireRequest dequeue() {
        Object response = redisTemplate.opsForList().leftPop(PAYMENT_QUEUE);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return parsePaymentRequest(response);
    }

    public PaymentWireRequest blockingDequeue() {
        Object result = redisTemplate.opsForList()
                .leftPop(PAYMENT_QUEUE, 5, TimeUnit.SECONDS);
        return parsePaymentRequest(result);
    }

    private PaymentWireRequest parsePaymentRequest(Object result) {
        if (result instanceof String) {
            try {
                return objectMapper.readValue((String) result, PaymentWireRequest.class);
            } catch (Exception e) {
                System.err.println("Erro ao fazer parse do pagamento: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public long getQueueSize() {
        return redisTemplate.opsForList().size(PAYMENT_QUEUE);
    }
}
