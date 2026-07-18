package com.govos.srh.production;

import com.govos.srh.exception.SearchEngineException;

import java.util.function.Supplier;

public class OpenSearchRetryExecutor {

    private final SearchResilienceProperties properties;

    public OpenSearchRetryExecutor(SearchResilienceProperties properties) {
        this.properties = properties;
    }

    public <T> T execute(String operation, Supplier<T> action) {
        int attempts = 0;
        long backoffMs = properties.getInitialBackoffMs();
        RuntimeException lastFailure = null;

        while (attempts <= properties.getMaxRetries()) {
            try {
                return action.get();
            } catch (SearchEngineException ex) {
                lastFailure = ex;
                if (attempts >= properties.getMaxRetries() || !isRetryable(ex)) {
                    throw ex;
                }
                sleep(backoffMs);
                backoffMs = Math.min(
                        (long) (backoffMs * properties.getBackoffMultiplier()),
                        properties.getMaxBackoffMs());
                attempts++;
            } catch (RuntimeException ex) {
                throw wrap(operation, ex);
            }
        }

        throw lastFailure != null ? lastFailure : wrap(operation, new IllegalStateException("Retry loop exhausted"));
    }

    public void executeVoid(String operation, Runnable action) {
        execute(operation, () -> {
            action.run();
            return null;
        });
    }

    private static boolean isRetryable(SearchEngineException ex) {
        Throwable cause = ex.getCause();
        if (cause == null) {
            return true;
        }
        String message = cause.getMessage();
        return message == null
                || message.contains("timeout")
                || message.contains("Timeout")
                || message.contains("Connection")
                || message.contains("503")
                || message.contains("429");
    }

    private static SearchEngineException wrap(String operation, Exception ex) {
        if (ex instanceof SearchEngineException searchEngineException) {
            return searchEngineException;
        }
        return new SearchEngineException("OpenSearch operation failed: " + operation, ex);
    }

    private static void sleep(long backoffMs) {
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new SearchEngineException("OpenSearch retry interrupted", interrupted);
        }
    }
}
