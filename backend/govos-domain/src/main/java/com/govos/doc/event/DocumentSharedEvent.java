package com.govos.doc.event;

import com.govos.doc.enums.ShareType;

import java.time.Instant;
import java.util.UUID;

public record DocumentSharedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID shareId,
        ShareType shareType,
        UUID sharedWithUserId,
        UUID sharedWithRoleId,
        String permission,
        Instant expiresAt
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.DOCUMENT_SHARED;
    }
}
