package com.rinha_de_backend_2025.services;

import com.rinha_de_backend_2025.models.PaymentWireRequest;
import com.rinha_de_backend_2025.models.PaymentSummary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class PaymentService {

    private final PaymentQueueService paymentQueueService;
    private final PaymentsProcessedService paymentsProcessedService;

    public PaymentService(PaymentQueueService paymentQueueService, 
                         PaymentsProcessedService paymentsProcessedService) {
        this.paymentQueueService = paymentQueueService;
        this.paymentsProcessedService = paymentsProcessedService;
    }

    public void processPayment(PaymentWireRequest payment) {
        Instant now = Instant.now();
        ZonedDateTime nowInSaoPaulo = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));

        PaymentWireRequest updatedPayment = new PaymentWireRequest(
                payment.correlationId(),
                payment.amount(),
                nowInSaoPaulo
        );

        // Apenas enfileira o pagamento para processamento assíncrono
        paymentQueueService.enqueue(updatedPayment);
    }

    public PaymentSummary getPaymentSummary(ZonedDateTime from, ZonedDateTime to) {
        PaymentsProcessedService.PaymentSummaryData data = paymentsProcessedService.getPaymentSummary(from, to);
        
        return new PaymentSummary(
            new PaymentSummary.DefaultProcessor(data.defaultTotalRequests, data.defaultTotalAmount),
            new PaymentSummary.FallbackProcessor(data.fallbackTotalRequests, data.fallbackTotalAmount)
        );
    }
}
