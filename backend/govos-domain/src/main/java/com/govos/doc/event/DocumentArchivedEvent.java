package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentArchivedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_ARCHIVED;
    }
}
