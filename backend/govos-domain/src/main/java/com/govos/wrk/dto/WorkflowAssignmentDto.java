package com.govos.wrk.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkflowAssignmentDto(
        UUID id,
        String code,
        UUID workflowTaskId,
        UUID userId,
        Instant assignedDate,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
