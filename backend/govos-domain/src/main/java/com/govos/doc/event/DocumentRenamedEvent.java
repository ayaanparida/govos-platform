package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentRenamedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        String title
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_RENAMED;
    }
}
