package com.govos.doc.entity;

import com.govos.doc.enums.AccessOperation;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentAccessLogTest {

    @Test
    void shouldExposeFixtureDefaults() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentAccessLog entity = DocumentTestFixtures.accessLog(DocumentTestFixtures.SHARE_ID, document);

        assertThat(entity.getDocument()).isSameAs(document);
        assertThat(entity.getUserId()).isEqualTo(DocumentTestFixtures.USER_ID);
        assertThat(entity.getOperation()).isEqualTo(AccessOperation.DOWNLOAD);
        assertThat(entity.getAccessedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(entity.getSuccess()).isTrue();
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldUpdateAuditFieldsViaSetters() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentAccessLog entity = new DocumentAccessLog();

        entity.setDocument(document);
        entity.setUserId(DocumentTestFixtures.OWNER_ID);
        entity.setOperation(AccessOperation.PREVIEW);
        entity.setAccessedAt(Instant.parse("2026-02-01T00:00:00Z"));
        entity.setSuccess(false);
        entity.setIpAddress("127.0.0.1");
        entity.setUserAgent("JUnit");
        entity.setDetails("failed");
        entity.setDeleted(true);
        entity.setVersion(1L);

        assertThat(entity.getOperation()).isEqualTo(AccessOperation.PREVIEW);
        assertThat(entity.getSuccess()).isFalse();
        assertThat(entity.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(entity.getUserAgent()).isEqualTo("JUnit");
        assertThat(entity.getDetails()).isEqualTo("failed");
        assertThat(entity.getDeleted()).isTrue();
        assertThat(entity.getVersion()).isEqualTo(1L);
    }
}
