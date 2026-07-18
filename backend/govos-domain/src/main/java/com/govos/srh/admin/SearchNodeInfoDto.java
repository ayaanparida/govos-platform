package com.govos.srh.admin;

public record SearchNodeInfoDto(
        String nodeId,
        String name,
        String host,
        String status,
        Long diskUsedBytes,
        Long memoryUsedBytes,
        Double cpuUsagePercent
) {
}
