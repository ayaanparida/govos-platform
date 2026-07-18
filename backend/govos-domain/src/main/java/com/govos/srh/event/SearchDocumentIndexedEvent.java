package com.govos.srh.event;

import com.govos.srh.enums.SearchDocumentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search document is indexed (SRH-008).
 */
public record SearchDocumentIndexedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt,
        UUID searchIndexId,
        UUID referenceId,
        String entityType,
        SearchDocumentStatus status
) {
}
