package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record StorageProviderDefaultChangedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID providerId,
        String providerName
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.STORAGE_PROVIDER_DEFAULT_CHANGED;
    }
}
