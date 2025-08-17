package com.rinha_de_backend_2025.models;

import java.util.Map;

public record PaymentSummary(
        int totalCount,
        double totalAmount,
        Map<String, Double> totalByType
) {}
