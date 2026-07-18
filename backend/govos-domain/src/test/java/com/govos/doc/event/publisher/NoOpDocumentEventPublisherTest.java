package com.govos.doc.event.publisher;

import com.govos.doc.event.DocumentCreatedEvent;
import com.govos.doc.event.DocumentEventTypes;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class NoOpDocumentEventPublisherTest {

    private final NoOpDocumentEventPublisher publisher = new NoOpDocumentEventPublisher();

    @Test
    void shouldPublishWithoutThrowing() {
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                DocumentTestFixtures.ORG_ID,
                DocumentTestFixtures.DOCUMENT_ID,
                DocumentTestFixtures.OWNER_ID,
                null,
                0L,
                "Title",
                "DOC-001",
                DocumentStatus.UPLOADED,
                DocumentClassification.INTERNAL,
                null,
                null);

        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
        assertThat(event.eventType()).isEqualTo(DocumentEventTypes.DOCUMENT_CREATED);
    }

    @Test
    void shouldPublishAllWithoutThrowing() {
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                DocumentTestFixtures.ORG_ID,
                DocumentTestFixtures.DOCUMENT_ID,
                DocumentTestFixtures.OWNER_ID,
                null,
                0L,
                "Title",
                "DOC-001",
                DocumentStatus.UPLOADED,
                DocumentClassification.INTERNAL,
                null,
                null);

        assertThatCode(() -> publisher.publishAll(List.of(event))).doesNotThrowAnyException();
    }

    @Test
    void shouldIgnoreNullOrEmptyPublishAll() {
        assertThatCode(() -> publisher.publishAll(null)).doesNotThrowAnyException();
        assertThatCode(() -> publisher.publishAll(Collections.emptyList())).doesNotThrowAnyException();
    }
}
