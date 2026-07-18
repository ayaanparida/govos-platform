package com.govos.doc.event;

import com.govos.doc.enums.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentRestoredEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        DocumentStatus status
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_RESTORED;
    }
}
