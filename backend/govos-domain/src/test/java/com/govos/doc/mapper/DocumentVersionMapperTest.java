package com.govos.doc.mapper;

import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.dto.version.UpdateDocumentVersionRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.OcrStatus;
import com.govos.doc.enums.PreviewStatus;
import com.govos.doc.enums.VirusScanStatus;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentVersionMapperTest {

    private DocumentVersionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentVersionMapperImpl();
    }

    @Test
    void shouldMapEntityToResponseFlatteningEmbeddedFields() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentVersion entity = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.VERSION_ID);
        assertThat(response.documentId()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
        assertThat(response.versionNumber()).isEqualTo(1);
        assertThat(response.versionLabel()).isEqualTo("v1");
        assertThat(response.checksum()).isEqualTo(DocumentTestFixtures.SHA256);
        assertThat(response.storageObjectKey()).isEqualTo(DocumentTestFixtures.STORAGE_KEY);
        assertThat(response.sizeBytes()).isEqualTo(1024L);
        assertThat(response.storageProviderId()).isEqualTo(DocumentTestFixtures.PROVIDER_ID);
    }

    @Test
    void shouldMapCreateRequestToEntityWithEmbeddedValueObjects() {
        CreateDocumentVersionRequest request = DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID);

        DocumentVersion entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getDocument()).isNull();
        assertThat(entity.getVersionNumber().getValue()).isEqualTo(1);
        assertThat(entity.getVersionNumber().getLabel()).isEqualTo("v1");
        assertThat(entity.getChecksum().getValue()).isEqualTo(DocumentTestFixtures.SHA256);
        assertThat(entity.getStorageLocation().getStorageObjectKey()).isEqualTo(DocumentTestFixtures.STORAGE_KEY);
        assertThat(entity.getFileSize().getSizeBytes()).isEqualTo(1024L);
    }

    @Test
    void shouldUpdateEntityIgnoringImmutableFields() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentVersion entity = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);
        String originalChecksum = entity.getChecksum().getValue();

        UpdateDocumentVersionRequest request = new UpdateDocumentVersionRequest(
                "updated-label", VirusScanStatus.CLEAN, OcrStatus.COMPLETED, PreviewStatus.READY,
                DocumentVersionStatus.ACTIVE, "preview", "thumb", 0L);
        mapper.updateEntity(request, entity);

        assertThat(entity.getChecksum().getValue()).isEqualTo(originalChecksum);
        assertThat(entity.getVersionNumber().getLabel()).isEqualTo("updated-label");
        assertThat(entity.getStorageLocation().getPreviewStorageKey()).isEqualTo("preview");
        assertThat(entity.getStorageLocation().getThumbnailStorageKey()).isEqualTo("thumb");
        assertThat(entity.getVirusScanStatus()).isEqualTo(VirusScanStatus.CLEAN);
    }

    @Test
    void shouldMapSummaryListAndPageHelpers() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentVersion entity = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);

        assertThat(mapper.toSummaryResponse(entity).versionNumber()).isEqualTo(1);
        assertThat(mapper.toResponseList(List.of(entity))).hasSize(1);
        assertThat(mapper.toSummaryResponseList(List.of(entity))).hasSize(1);
        assertThat(mapper.toSummaryResponsePage(new PageImpl<>(List.of(entity), PageRequest.of(0, 5), 1)))
                .hasSize(1);
    }
}
