package com.govos.api.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GovOS modular monolith module descriptor")
public record ModuleResponse(
        @Schema(description = "Maven module artifact name", example = "govos-security")
        String moduleName,
        @Schema(description = "Module version", example = "0.1.0-SNAPSHOT")
        String version,
        @Schema(description = "Operational status", example = "ACTIVE")
        String status
) {
}
