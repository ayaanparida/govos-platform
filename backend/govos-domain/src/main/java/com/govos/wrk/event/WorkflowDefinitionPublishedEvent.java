package com.govos.wrk.event;

import java.time.Instant;
import java.util.UUID;

public record WorkflowDefinitionPublishedEvent(
        UUID workflowDefinitionId,
        UUID workflowVersionId,
        Integer versionNumber,
        Instant occurredAt
) {
}
