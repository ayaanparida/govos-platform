package com.govos.srh.event;

import com.govos.srh.enums.SearchEngineType;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search index is created (SRH-008).
 */
public record SearchIndexCreatedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt,
        SearchEngineType engineType,
        Integer mappingVersion
) {
}
