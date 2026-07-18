package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowTaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateWorkflowTaskRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID workflowInstanceId,
        UUID assignedToId,
        UUID assignedRoleId,
        @NotNull
        UUID stepId,
        WorkflowTaskStatus status,
        Instant dueDate,
        Instant completedAt,
        Boolean active
) {
}
