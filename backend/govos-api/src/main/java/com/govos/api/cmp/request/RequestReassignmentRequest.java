package com.govos.api.cmp.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Officer assignment decline / reassignment request")
public record RequestReassignmentRequest(
        @NotBlank
        @Schema(description = "Reason key for declining the current assignment", example = "OVERLOADED")
        String rejectionReasonKey
) {
}
