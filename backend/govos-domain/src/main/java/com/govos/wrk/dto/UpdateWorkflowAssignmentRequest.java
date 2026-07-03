package com.govos.wrk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateWorkflowAssignmentRequest(
        @Size(max = 100)
        String code,
        @NotNull
        Instant assignedDate,
        Boolean active,
        Long version
) {
}
