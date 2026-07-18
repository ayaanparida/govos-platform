package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintDuplicateDetectedBy;
import com.govos.cmp.enums.ComplaintDuplicateStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ComplaintDuplicateDto(
        UUID id,
        String code,
        UUID primaryComplaintId,
        UUID duplicateComplaintId,
        ComplaintDuplicateDetectedBy detectedBy,
        UUID detectedByUserId,
        BigDecimal similarityScore,
        String remarks,
        ComplaintDuplicateStatus status,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
