package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowInstanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateWorkflowInstanceRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID workflowVersionId,
        @NotBlank @Size(max = 100)
        String referenceType,
        @NotNull
        UUID referenceId,
        WorkflowInstanceStatus status,
        Instant startedAt,
        Instant completedAt,
        Boolean active
) {
}
