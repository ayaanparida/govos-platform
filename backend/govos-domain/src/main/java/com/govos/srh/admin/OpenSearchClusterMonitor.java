package com.govos.srh.admin;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.nodes.Stats;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@Service("openSearchClusterMonitorDelegate")
public class OpenSearchClusterMonitor implements SearchClusterMonitor {

    private final OpenSearchClient openSearchClient;

    public OpenSearchClusterMonitor(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @Override
    public SearchClusterInfoDto getClusterInformation() {
        try {
            var response = openSearchClient.cluster().health();
            return new SearchClusterInfoDto(
                    response.clusterName(),
                    response.status() != null ? response.status().jsonValue() : "unknown",
                    safeInt(response.numberOfNodes()),
                    safeInt(response.activePrimaryShards()),
                    safeInt(response.activeShards()),
                    safeInt(response.relocatingShards()),
                    safeInt(response.initializingShards()),
                    safeInt(response.unassignedShards()));
        } catch (Exception ex) {
            throw new SearchAdministrationException("Failed to load OpenSearch cluster information", ex);
        }
    }

    @Override
    public List<SearchNodeInfoDto> getNodeInformation() {
        try {
            var response = openSearchClient.nodes().stats();
            if (response.nodes() == null || response.nodes().isEmpty()) {
                return List.of();
            }

            List<SearchNodeInfoDto> nodes = new ArrayList<>();
            for (Map.Entry<String, Stats> entry : response.nodes().entrySet()) {
                Stats stats = entry.getValue();
                Long diskUsed = null;
                Long memoryUsed = null;
                Double cpuUsage = null;

                if (stats.fs() != null && stats.fs().total() != null) {
                    long total = stats.fs().total().totalInBytes();
                    long available = stats.fs().total().availableInBytes();
                    diskUsed = Math.max(0L, total - available);
                }
                if (stats.jvm() != null && stats.jvm().mem() != null) {
                    memoryUsed = stats.jvm().mem().usedInBytes();
                }
                if (stats.os() != null && stats.os().cpu() != null) {
                    cpuUsage = (double) stats.os().cpu().percent();
                }

                nodes.add(new SearchNodeInfoDto(
                        entry.getKey(),
                        stats.name(),
                        stats.host(),
                        "online",
                        diskUsed,
                        memoryUsed,
                        cpuUsage));
            }
            return nodes;
        } catch (Exception ex) {
            throw new SearchAdministrationException("Failed to load OpenSearch node information", ex);
        }
    }

    @Override
    public SearchHealthDto getDetailedHealth(String engineStatus) {
        SearchClusterInfoDto cluster = getClusterInformation();
        List<SearchNodeInfoDto> nodes = getNodeInformation();

        long diskUsed = nodes.stream()
                .map(SearchNodeInfoDto::diskUsedBytes)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();
        long memoryUsed = nodes.stream()
                .map(SearchNodeInfoDto::memoryUsedBytes)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();
        OptionalDouble cpuAverageOptional = nodes.stream()
                .map(SearchNodeInfoDto::cpuUsagePercent)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average();
        Double cpuAverage = cpuAverageOptional.isPresent() ? cpuAverageOptional.getAsDouble() : null;

        int replicaShards = Math.max(0, cluster.activeShards() - cluster.activePrimaryShards());

        return new SearchHealthDto(
                engineStatus,
                cluster.numberOfNodes(),
                cluster.activePrimaryShards(),
                replicaShards,
                diskUsed > 0 ? diskUsed : null,
                null,
                memoryUsed > 0 ? memoryUsed : null,
                null,
                cpuAverage,
                Instant.now());
    }

    private static int safeInt(Number value) {
        return value != null ? value.intValue() : 0;
    }
}
