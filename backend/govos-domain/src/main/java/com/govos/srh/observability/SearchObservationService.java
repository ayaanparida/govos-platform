package com.govos.srh.observability;

import java.util.List;
import java.util.UUID;

public interface SearchObservationService {

    SearchObservabilitySnapshotDto getSnapshot();

    List<SearchTraceRecord> getRecentTraces(int limit);

    List<SearchObservationEvent> getRecentEvents(int limit);

    void publishEvent(SearchObservationEventType type, String operation, String status, long durationMs,
                      UUID organizationId, long documentCount, String provider, String engine);

    void recordTrace(SearchTraceRecord record);
}
