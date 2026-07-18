package com.govos.cmp.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ComplaintMergeCreateRequest(
        @NotNull
        UUID survivingComplaintId,
        @NotNull
        UUID mergedComplaintId,
        @NotNull
        UUID mergedByUserId,
        String mergeReason,
        @NotNull
        Instant mergedAt,
        Boolean active
) {
}
