package com.govos.srh.production;

import java.util.UUID;

public record SearchOperationContext(
        String operation,
        String status,
        long durationMs,
        UUID organizationId,
        String requestId,
        String indexCode,
        String entityType,
        UUID referenceId
) {
}
