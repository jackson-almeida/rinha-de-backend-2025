package com.rinha_de_backend_2025.config;

import com.rinha_de_backend_2025.component.PaymentWorker;
import com.rinha_de_backend_2025.services.PaymentQueueService;
import com.rinha_de_backend_2025.services.PaymentProcessorSelector;
import com.rinha_de_backend_2025.services.PaymentsProcessedService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WorkerConfig {

    @Value("${payment.workers.count:4}")
    private int workerCount;

    @Bean
    public ExecutorService paymentExecutor() {
        return Executors.newFixedThreadPool(workerCount);
    }

    @Bean
    public CommandLineRunner startWorkers(ExecutorService executorService, 
                                        PaymentQueueService paymentQueueService, 
                                        PaymentProcessorSelector processorSelector,
                                        RestTemplate restTemplate,
                                        PaymentsProcessedService paymentsProcessedService) {
        return args -> {
            System.out.println("Iniciando " + workerCount + " workers de pagamento...");
            for (int i = 0; i < workerCount; i++) {
                executorService.submit(new PaymentWorker(paymentQueueService, 
                                                       processorSelector, 
                                                       restTemplate, 
                                                       paymentsProcessedService));
            }
        };
    }
}
