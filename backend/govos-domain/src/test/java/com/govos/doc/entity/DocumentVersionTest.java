package com.govos.doc.entity;

import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.OcrStatus;
import com.govos.doc.enums.PreviewStatus;
import com.govos.doc.enums.VirusScanStatus;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.valueobject.DocumentChecksum;
import com.govos.doc.valueobject.FileSize;
import com.govos.doc.valueobject.StorageLocation;
import com.govos.doc.valueobject.VersionNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentVersionTest {

    @Test
    void shouldExposeEmbeddedValueObjectsAndPipelineDefaults() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentVersion entity = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);

        assertThat(entity.getDocument()).isSameAs(document);
        assertThat(entity.getVersionNumber().getValue()).isEqualTo(1);
        assertThat(entity.getVersionNumber().getLabel()).isEqualTo("v1");
        assertThat(entity.getChecksum().getValue()).isEqualTo(DocumentTestFixtures.SHA256);
        assertThat(entity.getStorageLocation().getStorageObjectKey()).isEqualTo(DocumentTestFixtures.STORAGE_KEY);
        assertThat(entity.getFileSize().getSizeBytes()).isEqualTo(1024L);
        assertThat(entity.getVersionStatus()).isEqualTo(DocumentVersionStatus.ACTIVE);
        assertThat(entity.getVirusScanStatus()).isEqualTo(VirusScanStatus.PENDING);
        assertThat(entity.getOcrStatus()).isEqualTo(OcrStatus.NOT_STARTED);
        assertThat(entity.getPreviewStatus()).isEqualTo(PreviewStatus.NOT_GENERATED);
        assertThat(entity.getImmutable()).isTrue();
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldUpdateFieldsViaSetters() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentVersion entity = new DocumentVersion();
        StorageProvider provider = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);

        entity.setDocument(document);
        entity.setVersionNumber(new VersionNumber(2, "v2"));
        entity.setChecksum(new DocumentChecksum(DocumentTestFixtures.SHA256));
        entity.setStorageLocation(new StorageLocation("key"));
        entity.getStorageLocation().setPreviewStorageKey("preview");
        entity.getStorageLocation().setThumbnailStorageKey("thumb");
        entity.setFileSize(new FileSize(2048L));
        entity.setMimeType("image/png");
        entity.setOriginalFilename("image.png");
        entity.setUploadedById(DocumentTestFixtures.USER_ID);
        entity.setStorageProvider(provider);
        entity.setVersionStatus(DocumentVersionStatus.SUPERSEDED);
        entity.setImmutable(false);
        entity.setDeleted(true);
        entity.setVersion(5L);

        assertThat(entity.getDocument()).isSameAs(document);
        assertThat(entity.getVersionNumber().getValue()).isEqualTo(2);
        assertThat(entity.getStorageProvider()).isSameAs(provider);
        assertThat(entity.getVersionStatus()).isEqualTo(DocumentVersionStatus.SUPERSEDED);
        assertThat(entity.getImmutable()).isFalse();
        assertThat(entity.getDeleted()).isTrue();
        assertThat(entity.getVersion()).isEqualTo(5L);
    }
}
