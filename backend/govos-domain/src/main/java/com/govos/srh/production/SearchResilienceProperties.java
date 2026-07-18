package com.govos.srh.production;

public class SearchResilienceProperties {

    private int maxRetries = 3;
    private long initialBackoffMs = 100L;
    private long maxBackoffMs = 2000L;
    private double backoffMultiplier = 2.0D;
    private long operationTimeoutMs = 5000L;
    private boolean gracefulDegradation = true;

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getInitialBackoffMs() {
        return initialBackoffMs;
    }

    public void setInitialBackoffMs(long initialBackoffMs) {
        this.initialBackoffMs = initialBackoffMs;
    }

    public long getMaxBackoffMs() {
        return maxBackoffMs;
    }

    public void setMaxBackoffMs(long maxBackoffMs) {
        this.maxBackoffMs = maxBackoffMs;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public long getOperationTimeoutMs() {
        return operationTimeoutMs;
    }

    public void setOperationTimeoutMs(long operationTimeoutMs) {
        this.operationTimeoutMs = operationTimeoutMs;
    }

    public boolean isGracefulDegradation() {
        return gracefulDegradation;
    }

    public void setGracefulDegradation(boolean gracefulDegradation) {
        this.gracefulDegradation = gracefulDegradation;
    }
}
