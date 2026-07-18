package com.govos.doc.mapper;

import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentMetadata;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMetadataMapperTest {

    private DocumentMetadataMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentMetadataMapperImpl();
    }

    @Test
    void shouldMapEntityToResponseFlatteningRelations() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentMetadata entity = DocumentTestFixtures.metadata(DocumentTestFixtures.METADATA_ID, document);
        entity.setDocumentVersion(DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document));

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.METADATA_ID);
        assertThat(response.documentId()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
        assertThat(response.documentVersionId()).isEqualTo(DocumentTestFixtures.VERSION_ID);
    }

    @Test
    void shouldUpdateEntityIgnoringDocumentRelations() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentMetadata entity = DocumentTestFixtures.metadata(DocumentTestFixtures.METADATA_ID, document);

        UpdateDocumentMetadataRequest request = DocumentTestFixtures.updateMetadataRequest();
        mapper.updateEntity(request, entity);

        assertThat(entity.getDocument()).isSameAs(document);
        assertThat(entity.getOcrText()).isEqualTo("ocr text");
        assertThat(entity.getOcrLanguage()).isEqualTo("en");
        assertThat(entity.getPageCount()).isEqualTo(1);
    }

    @Test
    void shouldIgnoreNullValuesOnUpdate() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentMetadata entity = DocumentTestFixtures.metadata(DocumentTestFixtures.METADATA_ID, document);
        entity.setOcrText("existing");

        UpdateDocumentMetadataRequest request = new UpdateDocumentMetadataRequest(
                null, null, null, null, null, null, null, null, null, 0L);
        mapper.updateEntity(request, entity);

        assertThat(entity.getOcrText()).isEqualTo("existing");
    }

    @Test
    void shouldMapResponseListAndHandleNullEntity() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentMetadata entity = DocumentTestFixtures.metadata(DocumentTestFixtures.METADATA_ID, document);

        assertThat(mapper.toResponseList(java.util.List.of(entity))).hasSize(1);
        assertThat(mapper.toResponse(null)).isNull();
    }
}
