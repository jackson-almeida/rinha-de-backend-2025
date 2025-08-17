package com.rinha_de_backend_2025.services;

import com.rinha_de_backend_2025.models.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentProcessorSelector {

    private final HealthStatusCache healthStatusCache;
    private final String defaultProcessorUrl;
    private final String fallbackProcessorUrl;

    public PaymentProcessorSelector(HealthStatusCache healthStatusCache,
                                   @Value("${payment.processor.default.url:http://payment-processor-default:8080}") String defaultProcessorUrl,
                                   @Value("${payment.processor.fallback.url:http://payment-processor-fallback:8080}") String fallbackProcessorUrl) {
        this.healthStatusCache = healthStatusCache;
        this.defaultProcessorUrl = defaultProcessorUrl;
        this.fallbackProcessorUrl = fallbackProcessorUrl;
    }

    /**
     * Seleciona o Payment Processor mais adequado baseado no status de saúde
     * @return URL do Payment Processor a ser usado
     */
    public String selectPaymentProcessor() {
        Optional<HealthStatus> healthStatus = healthStatusCache.getCachedStatus();
        
        // Se não há status no cache, assume que o default está funcionando
        if (healthStatus.isEmpty()) {
            return defaultProcessorUrl;
        }

        HealthStatus status = healthStatus.get();
        
        // Se o default não está falhando, usa ele
        if (!status.isFailing()) {
            return defaultProcessorUrl;
        }

        // Se o default está falhando, usa o fallback
        return fallbackProcessorUrl;
    }

    /**
     * Verifica se o Payment Processor Default está saudável
     * @return true se está saudável, false caso contrário
     */
    public boolean isDefaultProcessorHealthy() {
        Optional<HealthStatus> healthStatus = healthStatusCache.getCachedStatus();
        return healthStatus.isEmpty() || !healthStatus.get().isFailing();
    }

}
