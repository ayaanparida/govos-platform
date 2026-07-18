package com.govos.doc.event.publisher;

import com.govos.doc.event.DocumentDomainEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Default no-op publisher until infrastructure event adapters are wired (DOC-008).
 */
@Component
@Primary
public class NoOpDocumentEventPublisher implements DocumentEventPublisher {

    @Override
    public void publish(DocumentDomainEvent event) {
        // intentionally ignored until adapter milestone
    }

    @Override
    public void publishAll(Collection<DocumentDomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (DocumentDomainEvent event : events) {
            publish(event);
        }
    }
}
