package com.rinha_de_backend_2025.models;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class PaymentProcessed {
    private UUID correlationId;
    private BigDecimal amount;
    private ZonedDateTime requestedAt;
    private ZonedDateTime processedAt;
    private String processorUsed; // "default" ou "fallback"
    private boolean success;

    // Construtor padrão para serialização
    public PaymentProcessed() {}

    public PaymentProcessed(UUID correlationId, BigDecimal amount, ZonedDateTime requestedAt, 
                           ZonedDateTime processedAt, String processorUsed, boolean success) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.requestedAt = requestedAt;
        this.processedAt = processedAt;
        this.processorUsed = processorUsed;
        this.success = success;
    }

    // Getters e Setters
    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public ZonedDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(ZonedDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public ZonedDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(ZonedDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getProcessorUsed() {
        return processorUsed;
    }

    public void setProcessorUsed(String processorUsed) {
        this.processorUsed = processorUsed;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "PaymentProcessed{" +
                "correlationId=" + correlationId +
                ", amount=" + amount +
                ", requestedAt=" + requestedAt +
                ", processedAt=" + processedAt +
                ", processorUsed='" + processorUsed + '\'' +
                ", success=" + success +
                '}';
    }
}
