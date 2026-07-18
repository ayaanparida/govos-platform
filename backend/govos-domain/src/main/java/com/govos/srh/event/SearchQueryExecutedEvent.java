package com.govos.srh.event;

import com.govos.srh.enums.SearchQueryType;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search query is executed (SRH-008).
 */
public record SearchQueryExecutedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt,
        SearchQueryType queryType,
        String queryText,
        long executionTimeMs,
        long resultCount
) {
}
