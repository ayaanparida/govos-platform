package com.govos.wrk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateWorkflowTransitionRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID fromStepId,
        @NotNull
        UUID toStepId,
        @Size(max = 2000)
        String conditionExpression,
        Boolean active
) {
}
