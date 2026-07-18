package com.govos.srh.event;

import com.govos.srh.enums.SearchJobType;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search sync job completes (SRH-008).
 */
public record SearchSyncJobCompletedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt,
        UUID searchIndexId,
        SearchJobType jobType,
        long processedCount,
        long successCount,
        long failureCount
) {
}
