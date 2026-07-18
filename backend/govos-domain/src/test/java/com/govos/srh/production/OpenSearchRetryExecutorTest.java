package com.govos.srh.production;

import com.govos.srh.exception.SearchEngineException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenSearchRetryExecutorTest {

    private final SearchResilienceProperties properties = new SearchResilienceProperties();
    private final OpenSearchRetryExecutor executor = new OpenSearchRetryExecutor(properties);

    @Test
    void shouldRetryTransientFailures() {
        properties.setMaxRetries(2);
        AtomicInteger attempts = new AtomicInteger();

        int result = executor.execute("search", () -> {
            if (attempts.getAndIncrement() < 2) {
                throw new SearchEngineException("Connection reset", new RuntimeException("Connection reset"));
            }
            return 42;
        });

        assertThat(result).isEqualTo(42);
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldFailAfterMaxRetries() {
        properties.setMaxRetries(1);

        assertThatThrownBy(() -> executor.execute("search", () -> {
            throw new SearchEngineException("timeout", new RuntimeException("Read timed out"));
        })).isInstanceOf(SearchEngineException.class);
    }

    @Test
    void shouldReturnSuccessfulResultWithoutRetry() {
        String value = executor.execute("health", () -> "UP");
        assertThat(value).isEqualTo("UP");
    }
}
