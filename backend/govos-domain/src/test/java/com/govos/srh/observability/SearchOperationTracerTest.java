package com.govos.srh.observability;

import com.govos.srh.config.SearchProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchOperationTracerTest {

    private SearchOperationTracer tracer;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        SearchObservationProperties properties = new SearchObservationProperties();
        properties.setEnabled(true);
        properties.setSampleRate(1.0);
        meterRegistry = new SimpleMeterRegistry();
        SearchProperties searchProperties = new SearchProperties();
        SearchObservationEventStore eventStore = new SearchObservationEventStore(properties);
        @SuppressWarnings("unchecked")
        ObjectProvider<MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(meterRegistry);
        SearchObservationMetrics metrics = new SearchObservationMetrics(
                new com.govos.srh.production.SearchMetricsRecorder(provider, searchProperties),
                provider,
                properties);
        SearchObservationService observationService = new SearchObservationServiceImpl(
                properties,
                searchProperties,
                eventStore,
                metrics,
                new SearchTraceLogger(properties));
        @SuppressWarnings("unchecked")
        ObjectProvider<ObservationRegistry> observationProvider = mock(ObjectProvider.class);
        when(observationProvider.getIfAvailable(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ObservationRegistry.create());
        tracer = new SearchOperationTracer(
                properties,
                OpenTelemetry.noop().getTracer("test"),
                observationProvider,
                metrics,
                observationService,
                new SearchTraceLogger(properties));
    }

    @Test
    void shouldTraceSuccessfulOperation() {
        String result = tracer.trace(
                SearchSpanNames.SEARCH_QUERY,
                new SearchOperationTracer.TraceContextAttributes(
                        "search.query",
                        null,
                        0L,
                        "mock",
                        "opensearch",
                        SearchObservationEventType.SEARCH_STARTED,
                        SearchObservationEventType.SEARCH_COMPLETED,
                        SearchObservationEventType.SEARCH_FAILED),
                () -> "ok");

        assertThat(result).isEqualTo("ok");
        assertThat(meterRegistry.find("search.trace.created").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("search.trace.completed").counter().count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordFailureMetrics() {
        try {
            tracer.trace(
                    SearchSpanNames.SEARCH_QUERY,
                    new SearchOperationTracer.TraceContextAttributes(
                            "search.query",
                            null,
                            0L,
                            "mock",
                            "opensearch",
                            SearchObservationEventType.SEARCH_STARTED,
                            SearchObservationEventType.SEARCH_COMPLETED,
                            SearchObservationEventType.SEARCH_FAILED),
                    () -> {
                        throw new IllegalStateException("failed");
                    });
        } catch (IllegalStateException ignored) {
            // expected
        }

        assertThat(meterRegistry.find("search.trace.failed").counter().count()).isEqualTo(1.0);
    }

    @Test
    void shouldSkipTracingWhenDisabled() {
        SearchObservationProperties properties = new SearchObservationProperties();
        properties.setEnabled(false);
        SearchProperties searchProperties = new SearchProperties();
        SearchObservationEventStore eventStore = new SearchObservationEventStore(properties);
        SimpleMeterRegistry disabledRegistry = new SimpleMeterRegistry();
        @SuppressWarnings("unchecked")
        ObjectProvider<MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(disabledRegistry);
        SearchObservationMetrics metrics = new SearchObservationMetrics(
                new com.govos.srh.production.SearchMetricsRecorder(provider, searchProperties),
                provider,
                properties);
        SearchObservationService observationService = new SearchObservationServiceImpl(
                properties,
                searchProperties,
                eventStore,
                metrics,
                new SearchTraceLogger(properties));
        @SuppressWarnings("unchecked")
        ObjectProvider<ObservationRegistry> observationProvider = mock(ObjectProvider.class);
        when(observationProvider.getIfAvailable(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ObservationRegistry.create());
        SearchOperationTracer disabledTracer = new SearchOperationTracer(
                properties,
                OpenTelemetry.noop().getTracer("test"),
                observationProvider,
                metrics,
                observationService,
                new SearchTraceLogger(properties));

        String result = disabledTracer.trace(
                SearchSpanNames.SEARCH_QUERY,
                new SearchOperationTracer.TraceContextAttributes(
                        "search.query", null, 0L, "mock", "opensearch", null, null, null),
                () -> "ok");

        assertThat(result).isEqualTo("ok");
        assertThat(disabledRegistry.find("search.trace.created").counter()).isNull();
    }
}
