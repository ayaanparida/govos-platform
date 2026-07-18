package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentCategoryDeletedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID categoryId
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_CATEGORY_DELETED;
    }
}
