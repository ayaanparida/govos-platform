package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentMetadataUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID metadataId,
        UUID documentVersionId
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_METADATA_UPDATED;
    }
}
