package com.govos.doc.event;

import com.govos.doc.entity.DocumentAccessAction;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted when document access is logged.
 */
public record DocumentAccessLoggedEvent(
        UUID documentId,
        UUID userId,
        DocumentAccessAction action,
        Instant occurredAt
) {
}
