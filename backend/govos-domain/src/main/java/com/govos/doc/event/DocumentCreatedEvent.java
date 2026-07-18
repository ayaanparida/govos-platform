package com.govos.doc.event;

import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        String title,
        String documentNumber,
        DocumentStatus status,
        DocumentClassification classification,
        UUID folderId,
        UUID categoryId
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_CREATED;
    }
}
