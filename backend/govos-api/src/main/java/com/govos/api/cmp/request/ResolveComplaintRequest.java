package com.govos.api.cmp.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Resolution reason for a complaint")
public record ResolveComplaintRequest(
        @NotBlank
        @Schema(description = "MDM resolution reason key", example = "FIXED")
        String reasonKey
) {
}
