package com.govos.doc.event.publisher;

import com.govos.doc.event.DocumentDomainEvent;

import java.util.Collection;

/**
 * Abstraction for publishing DOC domain events (DOC-008).
 * Infrastructure adapters will replace the default no-op implementation in later milestones.
 */
public interface DocumentEventPublisher {

    void publish(DocumentDomainEvent event);

    void publishAll(Collection<DocumentDomainEvent> events);
}
