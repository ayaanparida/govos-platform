package com.govos.api.cmp.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Intake rejection reason for a complaint")
public record RejectComplaintRequest(
        @NotBlank
        @Schema(description = "MDM rejection reason key", example = "INVALID_CATEGORY")
        String rejectionReasonKey
) {
}
