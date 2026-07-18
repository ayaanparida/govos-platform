package com.govos.doc.event;

import com.govos.doc.enums.RetentionAction;

import java.time.Instant;
import java.util.UUID;

public record RetentionPolicyCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID organizationId,
        UUID documentId,
        UUID userId,
        String correlationId,
        Long version,
        UUID policyId,
        String name,
        Integer retentionDays,
        RetentionAction actionOnExpiry,
        Boolean legalHold
) implements DocumentDomainEvent {

    @Override
    public String eventType() {
        return DocumentEventTypes.RETENTION_POLICY_CREATED;
    }
}
