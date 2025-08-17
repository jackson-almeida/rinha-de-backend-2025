package com.rinha_de_backend_2025.services;

import com.rinha_de_backend_2025.models.PaymentWireRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentQueueService {
    private static final String PAYMENT_QUEUE = "payment_queue";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void enqueue(PaymentWireRequest request) {
        redisTemplate.opsForList().rightPush(PAYMENT_QUEUE, request);
    }

    public PaymentWireRequest dequeue() {
        Object response = redisTemplate.opsForList().leftPop(PAYMENT_QUEUE);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return (PaymentWireRequest) response;
    }

    public Object blockingDequeue() {
        System.out.println("## Antes do leftPop # Thread: " + Thread.currentThread().getName());
        Object result = redisTemplate.opsForList()
                .leftPop(PAYMENT_QUEUE, 5, TimeUnit.SECONDS); // timeout infinito
        System.out.println("# Depois do leftPop # Thread: " + Thread.currentThread().getName());
        System.out.println("# Result: " + result);
        return result;
    }

    public long getQueueSize() {
        return redisTemplate.opsForList().size(PAYMENT_QUEUE);
    }
}
