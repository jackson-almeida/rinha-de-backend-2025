package com.rinha_de_backend_2025.config;

import com.rinha_de_backend_2025.component.PaymentWorker;
import com.rinha_de_backend_2025.services.PaymentQueueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WorkerConfig {

    @Bean
    public ExecutorService paymentExecutor() {
        return Executors.newFixedThreadPool(1);
    }

    @Bean
    public CommandLineRunner startWorkers(ExecutorService executorService, PaymentQueueService paymentQueueService, RestTemplate restTemplate) {
        return args -> {
            for (int i = 0; i < 4; i++) {
                executorService.submit(new PaymentWorker(paymentQueueService, restTemplate));
            }
        };
    }
}
