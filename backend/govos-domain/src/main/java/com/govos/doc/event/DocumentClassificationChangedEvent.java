package com.govos.doc.event;

import com.govos.doc.enums.DocumentClassification;

import java.time.Instant;
import java.util.UUID;

public record DocumentClassificationChangedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        DocumentClassification classification
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_CLASSIFICATION_CHANGED;
    }
}
