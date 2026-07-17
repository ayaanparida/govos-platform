package com.govos.api.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated platform component health summary")
public record HealthResponse(
        @Schema(description = "Database health status", example = "UP")
        String database,
        @Schema(description = "Redis health status", example = "NOT_CONFIGURED")
        String redis,
        @Schema(description = "MinIO health status", example = "NOT_CONFIGURED")
        String minio,
        @Schema(description = "OpenSearch health status", example = "NOT_CONFIGURED")
        String opensearch,
        @Schema(description = "Disk space health status", example = "UP")
        String disk,
        @Schema(description = "JVM memory health status", example = "UP")
        String memory,
        @Schema(description = "Application uptime", example = "2h 15m 30s")
        String uptime
) {
}
