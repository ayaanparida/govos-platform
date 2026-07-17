package com.govos.api.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform runtime and build metadata")
public record PlatformInfoResponse(
        @Schema(description = "Spring application name", example = "govos-api")
        String applicationName,
        @Schema(description = "Application version", example = "0.1.0-SNAPSHOT")
        String version,
        @Schema(description = "Active Spring profile(s)", example = "local")
        String environment,
        @Schema(description = "Java runtime version", example = "21.0.11")
        String javaVersion,
        @Schema(description = "Spring Boot version", example = "3.5.16")
        String springBootVersion,
        @Schema(description = "Primary database product", example = "PostgreSQL")
        String database,
        @Schema(description = "Applied Flyway schema version", example = "1.7.0")
        String flywayVersion,
        @Schema(description = "Build timestamp from build-info", example = "2026-07-17T18:00:00Z")
        String buildTime,
        @Schema(description = "Git commit identifier", example = "c5af16c")
        String gitCommit
) {
}
