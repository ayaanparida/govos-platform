package com.govos.srh.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search index is activated (SRH-008).
 */
public record SearchIndexActivatedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt
) {
}
