package com.rinha_de_backend_2025.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinha_de_backend_2025.models.PaymentProcessed;
import com.rinha_de_backend_2025.models.PaymentWireRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PaymentsProcessedService {
    private static final String PAYMENTS_PROCESSED = "payments_processed";
    private static final String PAYMENTS_PROCESSED_DEFAULT = "payments_processed:default";
    private static final String PAYMENTS_PROCESSED_FALLBACK = "payments_processed:fallback";

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Adiciona um pagamento processado com sucesso
     */
    public void addPaymentProcessed(PaymentWireRequest request, String processorUsed, boolean success) {
        try {
            ZonedDateTime processedAt = ZonedDateTime.now();
            
            PaymentProcessed paymentProcessed = new PaymentProcessed(
                request.correlationId(),
                request.amount(),
                request.requestedAt(),
                processedAt,
                processorUsed,
                success
            );

            String json = objectMapper.writeValueAsString(paymentProcessed);
            
            // Armazena na lista geral
            redisTemplate.opsForList().rightPush(PAYMENTS_PROCESSED, json);
            
            // Armazena na lista específica do processor
            String processorKey = "default".equals(processorUsed) ? 
                PAYMENTS_PROCESSED_DEFAULT : PAYMENTS_PROCESSED_FALLBACK;
            redisTemplate.opsForList().rightPush(processorKey, json);
            
            System.out.println("Pagamento processado registrado: " + paymentProcessed);
        } catch (Exception e) {
            System.err.println("Erro ao registrar pagamento processado: " + e.getMessage());
        }
    }

    /**
     * Obtém pagamentos processados por período
     */
    public List<PaymentProcessed> getPaymentsByPeriod(ZonedDateTime from, ZonedDateTime to) {
        try {
            List<String> allPayments = redisTemplate.opsForList().range(PAYMENTS_PROCESSED, 0, -1);
            
            if (allPayments == null) {
                return List.of();
            }

            return allPayments.stream()
                .map(this::parsePaymentProcessed)
                .filter(payment -> payment != null)
                .filter(payment -> isInPeriod(payment.getProcessedAt(), from, to))
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erro ao buscar pagamentos por período: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Obtém estatísticas por processor no período
     */
    public PaymentSummaryData getPaymentSummary(ZonedDateTime from, ZonedDateTime to) {
        List<PaymentProcessed> payments = getPaymentsByPeriod(from, to);
        
        PaymentSummaryData summary = new PaymentSummaryData();
        
        for (PaymentProcessed payment : payments) {
            if (payment.isSuccess()) {
                if ("default".equals(payment.getProcessorUsed())) {
                    summary.defaultTotalRequests++;
                    summary.defaultTotalAmount = summary.defaultTotalAmount.add(payment.getAmount());
                } else if ("fallback".equals(payment.getProcessorUsed())) {
                    summary.fallbackTotalRequests++;
                    summary.fallbackTotalAmount = summary.fallbackTotalAmount.add(payment.getAmount());
                }
            }
        }
        
        return summary;
    }

    private PaymentProcessed parsePaymentProcessed(String json) {
        try {
            return objectMapper.readValue(json, PaymentProcessed.class);
        } catch (Exception e) {
            System.err.println("Erro ao fazer parse do pagamento: " + e.getMessage());
            return null;
        }
    }

    private boolean isInPeriod(ZonedDateTime date, ZonedDateTime from, ZonedDateTime to) {
        if (from != null && date.isBefore(from)) {
            return false;
        }
        if (to != null && date.isAfter(to)) {
            return false;
        }
        return true;
    }

    public PaymentProcessed blockingDequeue() {
        Object result = redisTemplate.opsForList().leftPop(PAYMENTS_PROCESSED, 30, TimeUnit.MICROSECONDS);
        if (result instanceof String) {
            return parsePaymentProcessed((String) result);
        }
        return null;
    }

    public long getQueueSize() {
        return redisTemplate.opsForList().size(PAYMENTS_PROCESSED);
    }

    /**
     * Classe interna para armazenar dados do resumo
     */
    public static class PaymentSummaryData {
        public int defaultTotalRequests = 0;
        public java.math.BigDecimal defaultTotalAmount = java.math.BigDecimal.ZERO;
        public int fallbackTotalRequests = 0;
        public java.math.BigDecimal fallbackTotalAmount = java.math.BigDecimal.ZERO;
    }
}
