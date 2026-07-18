package com.govos.api.cmp.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Reopen reason for a closed complaint")
public record ReopenComplaintRequest(
        @NotBlank
        @Schema(description = "MDM reopen reason key", example = "UNSATISFIED")
        String reasonKey
) {
}
