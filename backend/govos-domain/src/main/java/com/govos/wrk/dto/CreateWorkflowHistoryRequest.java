package com.govos.wrk.dto;

import com.govos.wrk.entity.WorkflowHistoryAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateWorkflowHistoryRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID workflowInstanceId,
        @NotNull
        WorkflowHistoryAction action,
        UUID performedById,
        @NotNull
        Instant performedAt,
        @Size(max = 2000)
        String remarks,
        Boolean active
) {
}
