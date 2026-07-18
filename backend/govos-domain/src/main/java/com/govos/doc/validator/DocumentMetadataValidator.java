package com.govos.doc.validator;

import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.exception.ValidationResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentMetadataValidator {

    private final Validator jakartaValidator;

    public DocumentMetadataValidator(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    public void validateDocumentScope(UUID documentId, UUID documentVersionId) {
        validateDocumentScopeResult(documentId, documentVersionId).throwIfInvalid();
    }

    public ValidationResult validateDocumentScopeResult(UUID documentId, UUID documentVersionId) {
        ValidationResult result = new ValidationResult();
        ValidationUtils.requireUuid(result, "documentId", documentId);
        if (documentVersionId != null && documentId == null) {
            result.addError("documentVersionId", "documentId is required when documentVersionId is provided");
        }
        return result;
    }

    public void validateUpdate(UpdateDocumentMetadataRequest request) {
        validateUpdateResult(request).throwIfInvalid();
    }

    public ValidationResult validateUpdateResult(UpdateDocumentMetadataRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "updateDocumentMetadataRequest");
        if (request == null) {
            result.addError("updateDocumentMetadataRequest", "Update document metadata request is required");
            return result;
        }
        ValidationUtils.requireNotNull(result, "version", request.version());
        if (request.ocrText() != null) {
            ValidationUtils.requireMaxLength(result, "ocrText", request.ocrText(), ValidationUtils.MAX_OCR_TEXT_LENGTH);
        }
        if (request.customAttributes() != null) {
            ValidationUtils.requireMaxLength(
                    result, "customAttributes", request.customAttributes(), ValidationUtils.MAX_METADATA_JSON_LENGTH);
        }
        if (request.extractedMetadata() != null) {
            ValidationUtils.requireMaxLength(
                    result, "extractedMetadata", request.extractedMetadata(), ValidationUtils.MAX_METADATA_JSON_LENGTH);
        }
        return result;
    }
}
