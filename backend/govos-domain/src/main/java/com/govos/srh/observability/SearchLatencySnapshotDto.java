package com.govos.srh.observability;

public record SearchLatencySnapshotDto(
        double queryLatencyMs,
        double semanticLatencyMs,
        double embeddingLatencyMs,
        double bulkLatencyMs,
        double schedulerLatencyMs,
        double providerLatencyMs) {
}
