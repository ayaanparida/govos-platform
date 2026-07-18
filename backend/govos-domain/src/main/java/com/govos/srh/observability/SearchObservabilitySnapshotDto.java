package com.govos.srh.observability;

public record SearchObservabilitySnapshotDto(
        boolean enabled,
        String exporter,
        String otlpEndpoint,
        double sampleRate,
        long traceCount,
        long eventCount,
        String activeProvider,
        String activeEngine) {
}
