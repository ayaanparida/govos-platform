package com.govos.doc.entity;

import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMetadataTest {

    @Test
    void shouldExposeFixtureDefaultsAndRelationships() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentMetadata entity = DocumentTestFixtures.metadata(DocumentTestFixtures.METADATA_ID, document);

        assertThat(entity.getDocument()).isSameAs(document);
        assertThat(entity.getOcrLanguage()).isEqualTo("en");
        assertThat(entity.getPageCount()).isEqualTo(1);
        assertThat(entity.getWatermarkApplied()).isFalse();
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldUpdateMetadataFieldsViaSetters() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentVersion version = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);
        DocumentMetadata entity = new DocumentMetadata();

        entity.setDocument(document);
        entity.setDocumentVersion(version);
        entity.setOcrText("text");
        entity.setOcrLanguage("fr");
        entity.setOcrConfidence(0.8);
        entity.setExtractedMetadata("{}");
        entity.setCustomAttributes("{}");
        entity.setPageCount(10);
        entity.setLanguageDetected("fr");
        entity.setWatermarkApplied(true);
        entity.setDeleted(true);
        entity.setVersion(4L);

        assertThat(entity.getDocumentVersion()).isSameAs(version);
        assertThat(entity.getOcrText()).isEqualTo("text");
        assertThat(entity.getOcrConfidence()).isEqualTo(0.8);
        assertThat(entity.getWatermarkApplied()).isTrue();
        assertThat(entity.getDeleted()).isTrue();
        assertThat(entity.getVersion()).isEqualTo(4L);
    }
}
