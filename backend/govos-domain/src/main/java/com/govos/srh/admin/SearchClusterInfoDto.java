package com.govos.srh.admin;

public record SearchClusterInfoDto(
        String clusterName,
        String status,
        int numberOfNodes,
        int activePrimaryShards,
        int activeShards,
        int relocatingShards,
        int initializingShards,
        int unassignedShards
) {
}
