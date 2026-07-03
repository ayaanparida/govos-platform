package com.govos.wrk.event;

import com.govos.wrk.entity.WorkflowInstanceStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowInstanceStartedEvent(
        UUID workflowInstanceId,
        String code,
        UUID workflowVersionId,
        String referenceType,
        UUID referenceId,
        WorkflowInstanceStatus status,
        Instant occurredAt
) {
}
