package com.govos.wrk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateWorkflowAssignmentRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID workflowTaskId,
        @NotNull
        UUID userId,
        @NotNull
        Instant assignedDate,
        Boolean active
) {
}
