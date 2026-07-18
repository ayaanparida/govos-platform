package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintDuplicateDetectedBy;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ComplaintDuplicateCreateRequest(
        @NotNull
        UUID primaryComplaintId,
        @NotNull
        UUID duplicateComplaintId,
        @NotNull
        ComplaintDuplicateDetectedBy detectedBy,
        UUID detectedByUserId,
        @DecimalMin("0") @DecimalMax("1")
        BigDecimal similarityScore,
        String remarks,
        Boolean active
) {
}
