package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionActivatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID versionId,
        Integer versionNumber
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_VERSION_ACTIVATED;
    }
}
