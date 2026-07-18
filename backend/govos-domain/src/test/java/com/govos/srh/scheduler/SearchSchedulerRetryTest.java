package com.govos.srh.scheduler;

import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class SearchSchedulerRetryTest {

    @Test
    void shouldRetryUntilSuccess() {
        SearchProperties properties = new SearchProperties();
        properties.getScheduler().setMaxRetries(2);
        properties.getScheduler().setInitialBackoffMs(1L);
        SearchSchedulerMetrics metrics = new SearchSchedulerMetrics(
                properties,
                new com.govos.srh.production.SearchMetricsRecorder(
                        org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class),
                        properties));
        SearchSchedulerRetryExecutor executor = new SearchSchedulerRetryExecutor(metrics);
        AtomicInteger attempts = new AtomicInteger();

        executor.execute("retry-test", () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("temporary");
            }
        });

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldFailAfterMaxRetries() {
        SearchProperties properties = new SearchProperties();
        properties.getScheduler().setMaxRetries(1);
        properties.getScheduler().setInitialBackoffMs(1L);
        SearchSchedulerMetrics metrics = new SearchSchedulerMetrics(
                properties,
                new com.govos.srh.production.SearchMetricsRecorder(
                        org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class),
                        properties));
        SearchSchedulerRetryExecutor executor = new SearchSchedulerRetryExecutor(metrics);

        assertThatThrownBy(() -> executor.execute("retry-test", () -> {
            throw new IllegalStateException("permanent");
        })).isInstanceOf(IllegalStateException.class);
    }
}
