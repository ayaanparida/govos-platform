package com.govos.srh.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchMonitoringServiceTest {

    private SearchMonitoringService monitoringService;
    private SearchObservationEventStore eventStore;

    @BeforeEach
    void setUp() {
        SearchObservationProperties properties = new SearchObservationProperties();
        properties.setEnabled(true);
        eventStore = new SearchObservationEventStore(properties);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        @SuppressWarnings("unchecked")
        ObjectProvider<MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(meterRegistry);
        monitoringService = new SearchMonitoringServiceImpl(
                eventStore,
                provider,
                properties);
    }

    @Test
    void shouldReturnMetricsSnapshot() {
        eventStore.addTrace(successTrace("search.query"));
        eventStore.addTrace(errorTrace("search.semantic"));

        SearchMetricsSnapshotDto snapshot = monitoringService.getMetricsSnapshot();

        assertThat(snapshot.totalTraces()).isEqualTo(2);
        assertThat(snapshot.completedTraces()).isEqualTo(1);
        assertThat(snapshot.failedTraces()).isEqualTo(1);
    }

    @Test
    void shouldReturnErrorSnapshot() {
        eventStore.addTrace(errorTrace("search.query"));
        eventStore.addTrace(errorTrace("search.query"));

        SearchErrorSnapshotDto snapshot = monitoringService.getErrorSnapshot();

        assertThat(snapshot.totalErrors()).isEqualTo(2);
        assertThat(snapshot.topFailedOperations()).contains("search.query:2");
    }

    @Test
    void shouldReturnLatencySnapshot() {
        SearchLatencySnapshotDto snapshot = monitoringService.getLatencySnapshot();
        assertThat(snapshot.queryLatencyMs()).isGreaterThanOrEqualTo(0.0);
    }

    private static SearchTraceRecord successTrace(String operation) {
        return trace(operation, "SUCCESS");
    }

    private static SearchTraceRecord errorTrace(String operation) {
        return trace(operation, "ERROR");
    }

    private static SearchTraceRecord trace(String operation, String status) {
        return new SearchTraceRecord(
                java.util.UUID.randomUUID(),
                "trace",
                "span",
                null,
                operation,
                status,
                10L,
                null,
                null,
                null,
                0L,
                "mock",
                "opensearch",
                java.time.Instant.now(),
                java.time.Instant.now());
    }
}
