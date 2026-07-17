package com.govos.api.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Build and source control metadata")
public record BuildResponse(
        @Schema(description = "Built artifact identifier", example = "govos-api")
        String artifact,
        @Schema(description = "CI or build number", example = "local")
        String buildNumber,
        @Schema(description = "Git branch name", example = "feature/platform-foundation")
        String gitBranch,
        @Schema(description = "Git commit hash", example = "c5af16c")
        String gitCommit,
        @Schema(description = "Build timestamp", example = "2026-07-17T18:00:00Z")
        String buildTimestamp
) {
}
