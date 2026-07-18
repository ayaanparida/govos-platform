package com.govos.srh.observability;

import java.util.UUID;

public record SearchTraceLogContext(
        String operation,
        String status,
        long durationMs,
        String traceId,
        String spanId,
        UUID organizationId,
        long documentCount,
        String provider,
        String engine) {
}
