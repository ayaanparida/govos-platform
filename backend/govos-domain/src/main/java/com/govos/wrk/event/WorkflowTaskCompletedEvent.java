package com.govos.wrk.event;

import com.govos.wrk.entity.WorkflowTaskStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowTaskCompletedEvent(
        UUID workflowTaskId,
        UUID workflowInstanceId,
        UUID stepId,
        WorkflowTaskStatus status,
        Instant completedAt,
        Instant occurredAt
) {
}
