package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowHistoryAction;

import java.time.Instant;
import java.util.UUID;

public record WorkflowHistoryDto(
        UUID id,
        String code,
        UUID workflowInstanceId,
        WorkflowHistoryAction action,
        UUID performedById,
        Instant performedAt,
        String remarks,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
