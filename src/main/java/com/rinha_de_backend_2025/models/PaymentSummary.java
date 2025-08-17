package com.rinha_de_backend_2025.models;

import java.math.BigDecimal;

public record PaymentSummary(
        DefaultProcessor defaultProcessor,
        FallbackProcessor fallbackProcessor
) {
    public record DefaultProcessor(
            int totalRequests,
            BigDecimal totalAmount
    ) {}

    public record FallbackProcessor(
            int totalRequests,
            BigDecimal totalAmount
    ) {}
}
