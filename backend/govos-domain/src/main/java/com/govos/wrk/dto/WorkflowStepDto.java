package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowStepType;

import java.time.Instant;
import java.util.UUID;

public record WorkflowStepDto(
        UUID id,
        String code,
        UUID workflowVersionId,
        String stepName,
        WorkflowStepType stepType,
        Integer sequenceNumber,
        Integer slaHours,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
