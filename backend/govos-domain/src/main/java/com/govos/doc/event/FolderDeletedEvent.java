package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record FolderDeletedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID folderId
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.FOLDER_DELETED;
    }
}
