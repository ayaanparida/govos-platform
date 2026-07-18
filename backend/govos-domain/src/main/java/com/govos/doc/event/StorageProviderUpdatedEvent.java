package com.govos.doc.event;

import com.govos.doc.enums.StorageProviderType;

import java.time.Instant;
import java.util.UUID;

public record StorageProviderUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID providerId,
        String providerName,
        StorageProviderType providerType,
        Boolean active
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.STORAGE_PROVIDER_UPDATED;
    }
}
