package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowTaskStatus;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record UpdateWorkflowTaskRequest(
        @Size(max = 100)
        String code,
        UUID assignedToId,
        UUID assignedRoleId,
        WorkflowTaskStatus status,
        Instant dueDate,
        Instant completedAt,
        Boolean active,
        Long version
) {
}
