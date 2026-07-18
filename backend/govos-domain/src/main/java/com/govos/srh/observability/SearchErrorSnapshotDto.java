package com.govos.srh.observability;

import java.util.List;

public record SearchErrorSnapshotDto(
        long totalErrors,
        double errorRate,
        List<String> topFailedOperations) {
}
