package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentCategoryUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID categoryId,
        String name,
        UUID parentCategoryId,
        UUID defaultRetentionPolicyId
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_CATEGORY_UPDATED;
    }
}
