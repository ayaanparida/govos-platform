package com.govos.srh.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search alias is activated (SRH-008).
 */
public record SearchAliasActivatedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt,
        UUID searchIndexId,
        String aliasName,
        String physicalIndexName
) {
}
