package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted when a new document version is recorded.
 */
public record DocumentVersionCreatedEvent(
        UUID documentId,
        UUID versionId,
        Integer versionNumber,
        Instant occurredAt
) {
}
