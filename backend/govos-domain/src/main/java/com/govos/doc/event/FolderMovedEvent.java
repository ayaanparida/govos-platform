package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record FolderMovedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID folderId,
        UUID parentFolderId,
        String materializedPath,
        Integer depthLevel
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.FOLDER_MOVED;
    }
}
