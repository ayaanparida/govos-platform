package com.govos.wrk.dto;

import jakarta.validation.constraints.Size;

public record UpdateWorkflowTransitionRequest(
        @Size(max = 100)
        String code,
        @Size(max = 2000)
        String conditionExpression,
        Boolean active,
        Long version
) {
}
