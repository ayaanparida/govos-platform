package com.govos.srh.production;

import java.time.Instant;

public record SearchOperationalHealthDto(
        String clusterStatus,
        int nodeCount,
        Long diskUsedBytes,
        Long heapUsedBytes,
        int pendingTasks,
        int unassignedShards,
        boolean indexAvailable,
        String semanticProviderHealth,
        String vectorIndexHealth,
        boolean semanticEnabled,
        String cacheHealth,
        long cacheEntries,
        Instant checkedAt
) {
}
