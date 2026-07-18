package com.govos.srh.scheduler;

import com.govos.srh.config.SearchProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchSchedulerMetricsTest {

    @Test
    void shouldRecordSchedulerMetrics() {
        SearchProperties properties = new SearchProperties();
        properties.getMetrics().setEnabled(true);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        @SuppressWarnings("unchecked")
        ObjectProvider<io.micrometer.core.instrument.MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(registry);

        SearchSchedulerMetrics metrics = new SearchSchedulerMetrics(
                properties,
                new com.govos.srh.production.SearchMetricsRecorder(provider, properties));

        metrics.recordExecution("daily-full-reindex");
        metrics.recordDuration("daily-full-reindex", 50L);
        metrics.recordFailure("daily-full-reindex");
        metrics.recordRetry("daily-full-reindex");
        metrics.recordSkipped("daily-full-reindex");

        assertThat(registry.find("scheduler.executions").counter()).isNotNull();
        assertThat(registry.find("scheduler.duration").timer()).isNotNull();
        assertThat(registry.find("scheduler.failures").counter()).isNotNull();
        assertThat(registry.find("scheduler.retries").counter()).isNotNull();
        assertThat(registry.find("scheduler.skipped").counter()).isNotNull();
    }
}
