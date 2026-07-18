package com.govos.srh.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search index is updated (SRH-008).
 */
public record SearchIndexUpdatedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt
) {
}
