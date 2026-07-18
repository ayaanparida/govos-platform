package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentCategoryCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID categoryId,
        String code,
        String name,
        UUID parentCategoryId
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_CATEGORY_CREATED;
    }
}
