package com.govos.srh.admin;

import java.time.Instant;

public record SearchHealthDto(
        String status,
        int nodeCount,
        int primaryShards,
        int replicaShards,
        Long diskUsedBytes,
        Long diskTotalBytes,
        Long memoryUsedBytes,
        Long memoryTotalBytes,
        Double cpuUsagePercent,
        Instant checkedAt
) {
}
