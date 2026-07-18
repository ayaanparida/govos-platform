package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentActiveVersionChangedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID activeVersionId,
        Integer activeVersionNumber
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_ACTIVE_VERSION_CHANGED;
    }
}
