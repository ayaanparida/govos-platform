package com.govos.wrk.event;

import com.govos.wrk.entity.WorkflowTaskStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowTaskAssignedEvent(
        UUID workflowTaskId,
        UUID workflowInstanceId,
        UUID stepId,
        UUID assignedToId,
        UUID assignedRoleId,
        WorkflowTaskStatus status,
        Instant occurredAt
) {
}
