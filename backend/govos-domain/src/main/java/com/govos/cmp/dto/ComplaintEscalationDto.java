package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;

import java.time.Instant;
import java.util.UUID;

public record ComplaintEscalationDto(
        UUID id,
        String code,
        UUID complaintId,
        ComplaintEscalationLevel escalationLevel,
        ComplaintEscalationReason escalationReason,
        UUID escalatedByUserId,
        UUID escalatedToUserId,
        UUID escalatedToDepartmentId,
        String remarks,
        Instant escalatedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
