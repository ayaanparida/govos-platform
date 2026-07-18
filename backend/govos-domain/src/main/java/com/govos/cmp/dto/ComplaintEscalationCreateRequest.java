package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ComplaintEscalationCreateRequest(
        @NotNull
        UUID complaintId,
        @NotNull
        ComplaintEscalationLevel escalationLevel,
        @NotNull
        ComplaintEscalationReason escalationReason,
        @NotNull
        UUID escalatedByUserId,
        UUID escalatedToUserId,
        UUID escalatedToDepartmentId,
        String remarks,
        @NotNull
        Instant escalatedAt,
        Boolean active
) {
}
