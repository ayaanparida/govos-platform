package com.govos.srh.scheduler;

import org.springframework.stereotype.Component;

@Component
public class SearchSchedulerRetryExecutor {

    private final SearchSchedulerProperties properties;
    private final SearchSchedulerMetrics metrics;

    public SearchSchedulerRetryExecutor(SearchSchedulerMetrics metrics) {
        this.metrics = metrics;
        this.properties = metrics.properties();
    }

    public void execute(String jobName, Runnable action) {
        int attempt = 0;
        long backoffMs = properties.getInitialBackoffMs();
        RuntimeException lastFailure = null;

        while (attempt <= properties.getMaxRetries()) {
            try {
                action.run();
                return;
            } catch (RuntimeException ex) {
                lastFailure = ex;
                if (attempt >= properties.getMaxRetries()) {
                    break;
                }
                metrics.recordRetry(jobName);
                sleep(backoffMs);
                backoffMs = Math.min(
                        (long) (backoffMs * properties.getBackoffMultiplier()),
                        properties.getMaxBackoffMs());
                attempt++;
            }
        }

        throw lastFailure != null
                ? lastFailure
                : new SearchSchedulerException("Scheduler retry exhausted for job: " + jobName);
    }

    private static void sleep(long backoffMs) {
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new SearchSchedulerException("Scheduler retry interrupted", interrupted);
        }
    }
}
