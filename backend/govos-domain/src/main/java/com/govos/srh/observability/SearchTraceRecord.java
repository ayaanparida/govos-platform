package com.govos.srh.observability;

import java.time.Instant;
import java.util.UUID;

public record SearchTraceRecord(
        UUID recordId,
        String traceId,
        String spanId,
        String parentSpanId,
        String operation,
        String status,
        long durationMs,
        UUID organizationId,
        String userId,
        String requestId,
        long documentCount,
        String provider,
        String engine,
        Instant startedAt,
        Instant completedAt) {
}
