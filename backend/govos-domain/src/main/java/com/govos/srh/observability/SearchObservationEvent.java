package com.govos.srh.observability;

import java.time.Instant;
import java.util.UUID;

public record SearchObservationEvent(
        UUID eventId,
        SearchObservationEventType type,
        String operation,
        String traceId,
        String spanId,
        String status,
        long durationMs,
        UUID organizationId,
        long documentCount,
        String provider,
        String engine,
        Instant timestamp) {
}
