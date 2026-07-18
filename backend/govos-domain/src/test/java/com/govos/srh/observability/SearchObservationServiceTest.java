package com.govos.srh.observability;

import com.govos.srh.config.SearchProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchObservationServiceTest {

    private SearchObservationService observationService;

    @BeforeEach
    void setUp() {
        SearchObservationProperties properties = new SearchObservationProperties();
        properties.setEnabled(true);
        SearchProperties searchProperties = new SearchProperties();
        SearchObservationEventStore eventStore = new SearchObservationEventStore(properties);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        @SuppressWarnings("unchecked")
        ObjectProvider<MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(meterRegistry);
        SearchObservationMetrics metrics = new SearchObservationMetrics(
                new com.govos.srh.production.SearchMetricsRecorder(provider, searchProperties),
                provider,
                properties);
        observationService = new SearchObservationServiceImpl(
                properties,
                searchProperties,
                eventStore,
                metrics,
                new SearchTraceLogger(properties));
    }

    @Test
    void shouldReturnObservabilitySnapshot() {
        SearchObservabilitySnapshotDto snapshot = observationService.getSnapshot();

        assertThat(snapshot.enabled()).isTrue();
        assertThat(snapshot.exporter()).isEqualTo("otlp");
        assertThat(snapshot.activeEngine()).isEqualTo("opensearch");
    }

    @Test
    void shouldPublishAndRetrieveEvents() {
        observationService.publishEvent(
                SearchObservationEventType.SEARCH_STARTED,
                "search.query",
                "STARTED",
                0L,
                null,
                0L,
                "mock",
                "opensearch");

        assertThat(observationService.getRecentEvents(10)).hasSize(1);
    }

    @Test
    void shouldRecordAndRetrieveTraces() {
        observationService.recordTrace(new SearchTraceRecord(
                java.util.UUID.randomUUID(),
                "trace-1",
                "span-1",
                null,
                "search.query",
                "SUCCESS",
                12L,
                null,
                "user-1",
                "req-1",
                0L,
                "mock",
                "opensearch",
                java.time.Instant.now(),
                java.time.Instant.now()));

        assertThat(observationService.getRecentTraces(10)).hasSize(1);
    }
}
