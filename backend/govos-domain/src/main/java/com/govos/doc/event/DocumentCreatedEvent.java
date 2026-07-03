package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted when a document metadata record is created.
 */
public record DocumentCreatedEvent(
        UUID documentId,
        String code,
        UUID ownerId,
        Instant occurredAt
) {
}
