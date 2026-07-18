package com.govos.doc.event;

import com.govos.doc.enums.DocumentVersionStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID versionId,
        Integer versionNumber,
        String mimeType,
        DocumentVersionStatus versionStatus,
        UUID storageProviderId
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_VERSION_CREATED;
    }
}
