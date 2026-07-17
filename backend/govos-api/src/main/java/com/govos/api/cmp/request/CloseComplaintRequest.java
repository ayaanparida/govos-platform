package com.govos.api.cmp.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Closure reason for a complaint")
public record CloseComplaintRequest(
        @NotBlank
        @Schema(description = "MDM closure reason key", example = "SATISFIED")
        String closureReasonKey
) {
}
