package com.rinha_de_backend_2025.models;

public class HealthStatus {
    private boolean failing;
    private long minResponseTime;

    // Construtor padrão para serialização
    public HealthStatus() {}

    public HealthStatus(boolean failing, long minResponseTime) {
        this.failing = failing;
        this.minResponseTime = minResponseTime;
    }

    public boolean isFailing() {
        return failing;
    }

    public void setFailing(boolean failing) {
        this.failing = failing;
    }

    public long getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(long minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    @Override
    public String toString() {
        return "HealthStatus{" +
                "failing=" + failing +
                ", minResponseTime=" + minResponseTime +
                '}';
    }
}
