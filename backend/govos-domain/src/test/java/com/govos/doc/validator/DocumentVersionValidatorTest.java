package com.govos.doc.validator;

import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.dto.version.UpdateDocumentVersionRequest;
import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.OcrStatus;
import com.govos.doc.enums.PreviewStatus;
import com.govos.doc.enums.VirusScanStatus;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentVersionValidatorTest {

    private DocumentVersionValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new DocumentVersionValidator(jakartaValidator);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        assertThatCode(() -> validator.validateCreate(
                DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenChecksumInvalid() {
        CreateDocumentVersionRequest request = DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID);
        CreateDocumentVersionRequest invalid = new CreateDocumentVersionRequest(
                request.documentId(), request.versionNumber(), request.versionLabel(), "not-a-hash",
                request.storageProviderId(), request.storageObjectKey(), request.previewStorageKey(),
                request.thumbnailStorageKey(), request.mimeType(), request.originalFilename(), request.sizeBytes(),
                request.uploadedById(), request.uploadedAt(), request.virusScanStatus(), request.ocrStatus(),
                request.previewStatus(), request.versionStatus());

        assertThatThrownBy(() -> validator.validateCreate(invalid))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenVersionNumberNotPositive() {
        CreateDocumentVersionRequest request = new CreateDocumentVersionRequest(
                DocumentTestFixtures.DOCUMENT_ID, 0, "v0", DocumentTestFixtures.SHA256,
                DocumentTestFixtures.PROVIDER_ID, DocumentTestFixtures.STORAGE_KEY, null, null,
                "application/pdf", "file.pdf", 1024L, DocumentTestFixtures.USER_ID,
                DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID).uploadedAt(),
                VirusScanStatus.PENDING, OcrStatus.NOT_STARTED, PreviewStatus.NOT_GENERATED,
                DocumentVersionStatus.ACTIVE);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        UpdateDocumentVersionRequest request = new UpdateDocumentVersionRequest(
                "v2", VirusScanStatus.CLEAN, OcrStatus.COMPLETED, PreviewStatus.READY,
                DocumentVersionStatus.ACTIVE, "preview-key", "thumb-key", 0L);

        assertThatCode(() -> validator.validateUpdate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUpdateWhenVersionMissing() {
        UpdateDocumentVersionRequest request = new UpdateDocumentVersionRequest(
                "v2", null, null, null, null, null, null, null);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(DocumentValidationException.class);
    }
}
