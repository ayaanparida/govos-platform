package com.govos.srh.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a search document is archived (SRH-008).
 */
public record SearchDocumentArchivedEvent(
        UUID aggregateId,
        String code,
        UUID organizationId,
        UUID changedByUserId,
        Instant occurredAt
) {
}
