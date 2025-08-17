package com.rinha_de_backend_2025.controllers;

import com.rinha_de_backend_2025.models.PaymentWireRequest;
import com.rinha_de_backend_2025.models.PaymentSummary;
import com.rinha_de_backend_2025.services.PaymentService;
import com.rinha_de_backend_2025.services.PaymentProcessorSelector;
import com.rinha_de_backend_2025.services.HealthStatusCache;
import com.rinha_de_backend_2025.models.HealthStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

@RestController
@RequestMapping("payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentProcessorSelector processorSelector;
    private final HealthStatusCache healthStatusCache;

    public PaymentController(PaymentService paymentService, 
                           PaymentProcessorSelector processorSelector,
                           HealthStatusCache healthStatusCache) {
        this.paymentService = paymentService;
        this.processorSelector = processorSelector;
        this.healthStatusCache = healthStatusCache;
    }

    // Implementar endpoint POST com rota /payments que receba o pagamento que devera ser processado no serviço de payment processor
    @PostMapping()
    public ResponseEntity<Void> processPayment(@RequestBody PaymentWireRequest payment) {
        CompletableFuture.runAsync(() -> paymentService.processPayment(payment));
        return ResponseEntity.accepted().build();
    }

    // Implementar endpoint GET com rota /payments-summary que deve retornar um resumo dos pagamentos processados. (Será usado para auditar a aplicação e deve bater com os resultador do GET /admin/payments-summary)
    @GetMapping("/summary")
    public ResponseEntity<PaymentSummary> getPaymentSummary(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to) {
        
        PaymentSummary summary = paymentService.getPaymentSummary(from, to);
        return ResponseEntity.ok(summary);
    }

    // Endpoint de teste para verificar o status de saúde dos Payment Processors
    @GetMapping("/health-status")
    public ResponseEntity<String> getHealthStatus() {
        Optional<HealthStatus> status = healthStatusCache.getCachedStatus();
        String selectedProcessor = processorSelector.selectPaymentProcessor();
        
        String response = String.format(
            "Status de Saúde: %s\nPayment Processor Selecionado: %s\nDefault Saudável: %s",
            status.orElse(new HealthStatus(false, 0)),
            selectedProcessor,
            processorSelector.isDefaultProcessorHealthy()
        );
        
        return ResponseEntity.ok(response);
    }

    // Observações:
    // Para cada um dos dois endpoint de processamento de pagamento esxiste também um GET /payments/service-health que retorna se o serviço em questão está enfrentando falhas e qual é o tempo minimo de resposta para o processamento.
    // Porém esse serviço tem um tempo de espera de 5 segundos entre chamadas
}
