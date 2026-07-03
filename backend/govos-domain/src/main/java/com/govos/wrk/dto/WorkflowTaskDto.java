package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowTaskStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowTaskDto(
        UUID id,
        String code,
        UUID workflowInstanceId,
        UUID assignedToId,
        UUID assignedRoleId,
        UUID stepId,
        WorkflowTaskStatus status,
        Instant dueDate,
        Instant completedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
