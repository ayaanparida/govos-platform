package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

public record RetentionPolicyRestoredEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID policyId
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.RETENTION_POLICY_RESTORED;
    }
}
