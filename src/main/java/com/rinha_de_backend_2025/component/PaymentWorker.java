package com.rinha_de_backend_2025.component;

import com.rinha_de_backend_2025.models.PaymentWireRequest;
import com.rinha_de_backend_2025.services.PaymentQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentWorker implements Runnable {
    private final PaymentQueueService paymentQueueService;
    private final RestTemplate restTemplate;

    public PaymentWorker(PaymentQueueService paymentQueueService, RestTemplate restTemplate) {
        this.paymentQueueService = paymentQueueService;
        this.restTemplate = restTemplate;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("## Fazendo dequeue");
            Object request = paymentQueueService.blockingDequeue();
            System.out.println("## Passou do dequeue!!");
//            PaymentWireRequest request = paymentQueueService.dequeue();

            if (request != null) {
                processPayment(request);
            }
        }
    }

    private void processPayment(Object request) {
        System.out.println("Processando pagamento na thread: " + Thread.currentThread().getName());

        try {
            String url = "http://payment-processor-default:8080/payments";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Response do payment-processor: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Erro ao enviar pagamento: " + e.getMessage());
        }
    }
}
