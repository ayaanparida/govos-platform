package com.govos.srh.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search alias is soft-deleted (SRH-008).
 */
public record SearchAliasDeletedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt
) {
}
