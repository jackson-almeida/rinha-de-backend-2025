package com.rinha_de_backend_2025.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

public record PaymentWireRequest(
        UUID correlationId,
        BigDecimal amount,
        ZonedDateTime requestedAt
) { }
