package com.govos.wrk.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkflowTransitionDto(
        UUID id,
        String code,
        UUID fromStepId,
        UUID toStepId,
        String conditionExpression,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
