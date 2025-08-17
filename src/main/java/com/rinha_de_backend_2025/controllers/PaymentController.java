package com.rinha_de_backend_2025.controllers;

import com.rinha_de_backend_2025.models.PaymentWireRequest;
import com.rinha_de_backend_2025.models.PaymentSummary;
import com.rinha_de_backend_2025.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Implementar endpoint POST com rota /payments que receba o pagamento que devera ser processado no serviço de payment processor
    @PostMapping()
    public ResponseEntity<Void> processPayment(@RequestBody PaymentWireRequest payment) {
        CompletableFuture.runAsync(() -> paymentService.processPayment(payment));
        return ResponseEntity.accepted().build();
    }

    // Implementar endpoint GET com rota /payments-summary que deve retornar um resumo dos pagamentos processados. (Será usado para auditar a aplicação e deve bater com os resultador do GET /admin/payments-summary)
    @GetMapping("/summary")
    public ResponseEntity<PaymentSummary> processPayment() {
        PaymentSummary summary = paymentService.getPaymentSummary();
        return ResponseEntity.ok(summary);
    }

    // Observações:
    // Para cada um dos dois endpoint de processamento de pagamento esxiste também um GET /payments/service-health que retorna se o serviço em questão está enfrentando falhas e qual é o tempo minimo de resposta para o processamento.
    // Porém esse serviço tem um tempo de espera de 5 segundos entre chamadas
}
