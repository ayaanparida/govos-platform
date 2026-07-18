package com.govos.srh.observability;

public record SearchMetricsSnapshotDto(
        long totalTraces,
        long completedTraces,
        long failedTraces,
        long observationEvents,
        double errorRate,
        double throughputPerMinute) {
}
