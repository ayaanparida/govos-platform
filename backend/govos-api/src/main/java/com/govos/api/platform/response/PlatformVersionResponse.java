package com.govos.api.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform release version metadata")
public record PlatformVersionResponse(
        @Schema(description = "Semantic platform version", example = "0.1.0-SNAPSHOT")
        String version,
        @Schema(description = "Release label", example = "0.1.0")
        String release,
        @Schema(description = "Release date", example = "2026-07-17")
        String releaseDate
) {
}
