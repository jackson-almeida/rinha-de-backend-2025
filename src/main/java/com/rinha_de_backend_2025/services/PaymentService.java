package com.rinha_de_backend_2025.services;

import com.rinha_de_backend_2025.models.PaymentWireRequest;
import com.rinha_de_backend_2025.models.PaymentSummary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Service
public class PaymentService {

    private final PaymentQueueService paymentQueueService;

    public PaymentService(PaymentQueueService paymentQueueService) {
        this.paymentQueueService = paymentQueueService;
    }

    public void processPayment(PaymentWireRequest payment) {
        Instant now = Instant.now();
        ZonedDateTime nowInSaoPaulo = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));

        PaymentWireRequest updatedPayment = new PaymentWireRequest(
                payment.correlationId(),
                payment.amount(),
                nowInSaoPaulo
        );

        paymentQueueService.enqueue(updatedPayment);
    }

    public PaymentSummary getPaymentSummary() {
        return new PaymentSummary(0, 0.0, Map.of());
    }
}
