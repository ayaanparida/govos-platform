package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record FolderUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID folderId,
        String name,
        String materializedPath,
        Integer depthLevel
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.FOLDER_UPDATED;
    }
}
