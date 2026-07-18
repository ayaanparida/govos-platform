package com.govos.doc.validator;

import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.dto.version.UpdateDocumentVersionRequest;
import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.OcrStatus;
import com.govos.doc.enums.PreviewStatus;
import com.govos.doc.enums.VirusScanStatus;
import com.govos.doc.exception.ValidationResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class DocumentVersionValidator {

    private final Validator jakartaValidator;

    public DocumentVersionValidator(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    public void validateCreate(CreateDocumentVersionRequest request) {
        validateVersion(request);
    }

    public void validateVersion(CreateDocumentVersionRequest request) {
        validateCreateResult(request).throwIfInvalid();
    }

    public ValidationResult validateCreateResult(CreateDocumentVersionRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "createDocumentVersionRequest");
        if (request == null) {
            result.addError("createDocumentVersionRequest", "Create document version request is required");
            return result;
        }
        ValidationUtils.requireUuid(result, "documentId", request.documentId());
        ValidationUtils.requirePositive(result, "versionNumber", request.versionNumber());
        ValidationUtils.requireSha256Checksum(result, "checksum", request.checksum());
        ValidationUtils.requireText(result, "storageObjectKey", request.storageObjectKey());
        ValidationUtils.requireMaxLength(
                result, "storageObjectKey", request.storageObjectKey(), ValidationUtils.MAX_STORAGE_KEY_LENGTH);
        ValidationUtils.requirePositive(result, "sizeBytes", request.sizeBytes());
        ValidationUtils.requireMimeType(result, "mimeType", request.mimeType());
        ValidationUtils.requireText(result, "originalFilename", request.originalFilename());
        ValidationUtils.requireMaxLength(
                result, "originalFilename", request.originalFilename(), ValidationUtils.MAX_FILENAME_LENGTH);
        ValidationUtils.requireUuid(result, "uploadedById", request.uploadedById());
        ValidationUtils.requireNotNull(result, "uploadedAt", request.uploadedAt());
        ValidationUtils.requireUuid(result, "storageProviderId", request.storageProviderId());
        validatePipelineStatuses(result, request.virusScanStatus(), request.ocrStatus(), request.previewStatus(),
                request.versionStatus());
        return result;
    }

    public void validateUpdate(UpdateDocumentVersionRequest request) {
        validateUpdateResult(request).throwIfInvalid();
    }

    public ValidationResult validateUpdateResult(UpdateDocumentVersionRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "updateDocumentVersionRequest");
        if (request == null) {
            result.addError("updateDocumentVersionRequest", "Update document version request is required");
            return result;
        }
        ValidationUtils.requireNotNull(result, "version", request.version());
        validatePipelineStatuses(result, request.virusScanStatus(), request.ocrStatus(), request.previewStatus(),
                request.versionStatus());
        if (request.previewStorageKey() != null) {
            ValidationUtils.requireMaxLength(
                    result, "previewStorageKey", request.previewStorageKey(), ValidationUtils.MAX_STORAGE_KEY_LENGTH);
        }
        if (request.thumbnailStorageKey() != null) {
            ValidationUtils.requireMaxLength(
                    result, "thumbnailStorageKey", request.thumbnailStorageKey(), ValidationUtils.MAX_STORAGE_KEY_LENGTH);
        }
        return result;
    }

    private void validatePipelineStatuses(
            ValidationResult result,
            VirusScanStatus virusScanStatus,
            OcrStatus ocrStatus,
            PreviewStatus previewStatus,
            DocumentVersionStatus versionStatus) {
        if (virusScanStatus != null) {
            ValidationUtils.requireEnum(result, "virusScanStatus", virusScanStatus);
        }
        if (ocrStatus != null) {
            ValidationUtils.requireEnum(result, "ocrStatus", ocrStatus);
        }
        if (previewStatus != null) {
            ValidationUtils.requireEnum(result, "previewStatus", previewStatus);
        }
        if (versionStatus != null) {
            ValidationUtils.requireEnum(result, "versionStatus", versionStatus);
        }
    }
}
