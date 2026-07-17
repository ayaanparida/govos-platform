package com.govos.api.cmp.request;

import com.govos.cmp.enums.ComplaintDuplicateDetectedBy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Mark a complaint as duplicate of a primary complaint")
public record MarkDuplicateRequest(
        @NotNull
        @Schema(description = "Primary (surviving) complaint identifier")
        UUID primaryComplaintId,
        @NotNull
        @Schema(description = "How the duplicate was detected", example = "MANUAL")
        ComplaintDuplicateDetectedBy detectedBy,
        @Schema(description = "Optional similarity score between 0 and 1")
        @DecimalMin("0") @DecimalMax("1")
        BigDecimal similarityScore,
        @Schema(description = "Optional duplicate link remarks")
        String remarks,
        @Schema(description = "Active flag override")
        Boolean active
) {
}
