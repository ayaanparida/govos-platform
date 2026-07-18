package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowInstanceStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowInstanceDto(
        UUID id,
        String code,
        UUID workflowVersionId,
        String referenceType,
        UUID referenceId,
        WorkflowInstanceStatus status,
        Instant startedAt,
        Instant completedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
