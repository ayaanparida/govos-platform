package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintMergeStatus;

import java.time.Instant;
import java.util.UUID;

public record ComplaintMergeDto(
        UUID id,
        String code,
        UUID survivingComplaintId,
        UUID mergedComplaintId,
        UUID mergedByUserId,
        String mergeReason,
        Instant mergedAt,
        ComplaintMergeStatus status,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
