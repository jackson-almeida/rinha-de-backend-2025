package com.rinha_de_backend_2025.component;

import com.rinha_de_backend_2025.models.PaymentWireRequest;
import com.rinha_de_backend_2025.services.PaymentQueueService;
import com.rinha_de_backend_2025.services.PaymentProcessorSelector;
import com.rinha_de_backend_2025.services.PaymentsProcessedService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentWorker implements Runnable {
    private final PaymentQueueService paymentQueueService;
    private final PaymentProcessorSelector processorSelector;
    private final RestTemplate restTemplate;
    private final PaymentsProcessedService paymentsProcessedService;

    public PaymentWorker(PaymentQueueService paymentQueueService, 
                        PaymentProcessorSelector processorSelector,
                        RestTemplate restTemplate,
                        PaymentsProcessedService paymentsProcessedService) {
        this.paymentQueueService = paymentQueueService;
        this.processorSelector = processorSelector;
        this.restTemplate = restTemplate;
        this.paymentsProcessedService = paymentsProcessedService;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("## Fazendo dequeue");
            PaymentWireRequest request = paymentQueueService.blockingDequeue();
            System.out.println("## Passou do dequeue!!");

            if (request != null) {
                processPayment(request);
            }
        }
    }

    private void processPayment(PaymentWireRequest request) {
        System.out.println("Processando pagamento na thread: " + Thread.currentThread().getName());

        try {
            // Seleciona o Payment Processor baseado no status de saúde
            String processorUrl = processorSelector.selectPaymentProcessor();
            String processorUsed = processorUrl.contains("default") ? "default" : "fallback";
            
            // Processa o pagamento
            boolean success = processPaymentWithProcessor(request, processorUrl);
            
            // Registra o pagamento processado
            paymentsProcessedService.addPaymentProcessed(request, processorUsed, success);
            
        } catch (Exception e) {
            System.err.println("Erro ao processar pagamento: " + e.getMessage());
            // Registra como falha
            paymentsProcessedService.addPaymentProcessed(request, "unknown", false);
        }
    }

    private boolean processPaymentWithProcessor(PaymentWireRequest payment, String processorUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<PaymentWireRequest> request = new HttpEntity<>(payment, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                processorUrl + "/payments", 
                request, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Pagamento processado com sucesso via " + processorUrl);
                return true;
            } else {
                System.err.println("Erro ao processar pagamento via " + processorUrl + ": " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Exceção ao processar pagamento via " + processorUrl + ": " + e.getMessage());
            return false;
        }
    }
}
