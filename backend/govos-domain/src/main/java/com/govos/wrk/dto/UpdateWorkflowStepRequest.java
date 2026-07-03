package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowStepType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateWorkflowStepRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String stepName,
        @NotNull
        WorkflowStepType stepType,
        @NotNull
        Integer sequenceNumber,
        Integer slaHours,
        Boolean active,
        Long version
) {
}
