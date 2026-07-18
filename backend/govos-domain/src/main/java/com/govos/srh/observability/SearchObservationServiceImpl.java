package com.govos.srh.observability;

import com.govos.srh.config.SearchProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SearchObservationServiceImpl implements SearchObservationService {

    private final SearchObservationProperties properties;
    private final SearchProperties searchProperties;
    private final SearchObservationEventStore eventStore;
    private final SearchObservationMetrics metrics;
    private final SearchTraceLogger traceLogger;

    public SearchObservationServiceImpl(
            SearchObservationProperties properties,
            SearchProperties searchProperties,
            SearchObservationEventStore eventStore,
            SearchObservationMetrics metrics,
            SearchTraceLogger traceLogger) {
        this.properties = properties;
        this.searchProperties = searchProperties;
        this.eventStore = eventStore;
        this.metrics = metrics;
        this.traceLogger = traceLogger;
    }

    @Override
    public SearchObservabilitySnapshotDto getSnapshot() {
        return new SearchObservabilitySnapshotDto(
                properties.isEnabled(),
                properties.getExporter(),
                properties.getOtlpEndpoint(),
                properties.getSampleRate(),
                eventStore.traceCount(),
                eventStore.eventCount(),
                searchProperties.getSemantic().getProvider(),
                "opensearch");
    }

    @Override
    public List<SearchTraceRecord> getRecentTraces(int limit) {
        return eventStore.recentTraces(limit);
    }

    @Override
    public List<SearchObservationEvent> getRecentEvents(int limit) {
        return eventStore.recentEvents(limit);
    }

    @Override
    public void publishEvent(
            SearchObservationEventType type,
            String operation,
            String status,
            long durationMs,
            UUID organizationId,
            long documentCount,
            String provider,
            String engine) {
        if (!properties.isEnabled()) {
            return;
        }
        SearchTraceContext context = SearchTraceContext.current();
        SearchObservationEvent event = new SearchObservationEvent(
                UUID.randomUUID(),
                type,
                operation,
                context.traceId(),
                context.spanId(),
                status,
                durationMs,
                organizationId,
                documentCount,
                provider,
                engine,
                Instant.now());
        eventStore.addEvent(event);
        metrics.recordObservationEvent(type.name());
        traceLogger.logEvent(type, operation, context.traceId());
    }

    @Override
    public void recordTrace(SearchTraceRecord record) {
        if (!properties.isEnabled()) {
            return;
        }
        eventStore.addTrace(record);
    }
}
