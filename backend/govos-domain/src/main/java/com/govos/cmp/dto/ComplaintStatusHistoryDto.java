package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

public record ComplaintStatusHistoryDto(
        UUID id,
        String code,
        UUID complaintId,
        ComplaintStatus fromStatus,
        ComplaintStatus toStatus,
        UUID changedByUserId,
        String reason,
        String reasonKey,
        Instant occurredAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
