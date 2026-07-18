package com.govos.doc.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Common contract for all Document Management domain events (DOC-008).
 */
public interface DocumentDomainEvent {

    UUID eventId();

    String eventType();

    Instant occurredAt();

    UUID organizationId();

    UUID documentId();

    UUID userId();

    String correlationId();

    Long version();
}
