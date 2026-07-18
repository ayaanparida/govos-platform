package com.govos.doc.validator;

import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.exception.ValidationResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class DocumentValidator {

    private static final Map<DocumentStatus, Set<DocumentStatus>> ALLOWED_TRANSITIONS = Map.of(
            DocumentStatus.UPLOADED, EnumSet.of(DocumentStatus.PROCESSING, DocumentStatus.ARCHIVED, DocumentStatus.DELETED),
            DocumentStatus.PROCESSING, EnumSet.of(DocumentStatus.READY, DocumentStatus.DELETED),
            DocumentStatus.READY, EnumSet.of(DocumentStatus.ARCHIVED, DocumentStatus.DELETED),
            DocumentStatus.ARCHIVED, EnumSet.of(DocumentStatus.READY, DocumentStatus.DELETED),
            DocumentStatus.DELETED, EnumSet.of(DocumentStatus.UPLOADED));

    private final Validator jakartaValidator;

    public DocumentValidator(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    public void validateCreate(CreateDocumentRequest request) {
        validateCreateResult(request).throwIfInvalid();
    }

    public ValidationResult validateCreateResult(CreateDocumentRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "createDocumentRequest");
        if (request == null) {
            result.addError("createDocumentRequest", "Create document request is required");
            return result;
        }
        ValidationUtils.requireText(result, "title", request.title());
        ValidationUtils.requireMaxLength(result, "title", request.title(), ValidationUtils.MAX_TITLE_LENGTH);
        ValidationUtils.requireUuid(result, "organizationId", request.organizationId());
        ValidationUtils.requireUuid(result, "ownerId", request.ownerId());
        ValidationUtils.requireEnum(result, "classification", request.classification());
        ValidationUtils.requireDocumentNumber(result, "documentNumber", request.documentNumber());
        if (request.mimeType() != null) {
            ValidationUtils.requireMimeType(result, "mimeType", request.mimeType());
        }
        return result;
    }

    public void validateUpdate(UpdateDocumentRequest request) {
        validateUpdateResult(request).throwIfInvalid();
    }

    public ValidationResult validateUpdateResult(UpdateDocumentRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "updateDocumentRequest");
        if (request == null) {
            result.addError("updateDocumentRequest", "Update document request is required");
            return result;
        }
        if (request.title() != null) {
            ValidationUtils.requireText(result, "title", request.title());
            ValidationUtils.requireMaxLength(result, "title", request.title(), ValidationUtils.MAX_TITLE_LENGTH);
        }
        if (request.documentNumber() != null) {
            ValidationUtils.requireDocumentNumber(result, "documentNumber", request.documentNumber());
        }
        if (request.mimeType() != null) {
            ValidationUtils.requireMimeType(result, "mimeType", request.mimeType());
        }
        ValidationUtils.requireNotNull(result, "version", request.version());
        return result;
    }

    public void validateStatusTransition(DocumentStatus currentStatus, DocumentStatus targetStatus) {
        validateStatusTransitionResult(currentStatus, targetStatus).throwIfInvalid();
    }

    public ValidationResult validateStatusTransitionResult(DocumentStatus currentStatus, DocumentStatus targetStatus) {
        ValidationResult result = new ValidationResult();
        ValidationUtils.requireEnum(result, "currentStatus", currentStatus);
        ValidationUtils.requireEnum(result, "targetStatus", targetStatus);
        if (!result.isValid()) {
            return result;
        }
        if (currentStatus == targetStatus) {
            return result;
        }
        Set<DocumentStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(targetStatus)) {
            result.addError(
                    "status",
                    "Illegal document status transition from " + currentStatus + " to " + targetStatus,
                    "DOC_INVALID_STATUS_TRANSITION");
        }
        return result;
    }

    public void validateDelete(UUID documentId) {
        validateDeleteResult(documentId).throwIfInvalid();
    }

    public ValidationResult validateDeleteResult(UUID documentId) {
        ValidationResult result = new ValidationResult();
        ValidationUtils.requireUuid(result, "documentId", documentId);
        return result;
    }

    public void validateRestore(DocumentStatus currentStatus) {
        validateRestoreResult(currentStatus).throwIfInvalid();
    }

    public ValidationResult validateRestoreResult(DocumentStatus currentStatus) {
        ValidationResult result = new ValidationResult();
        if (currentStatus != DocumentStatus.DELETED) {
            result.addError("status", "Restore requires document status DELETED", "DOC_INVALID_RESTORE");
        }
        return result;
    }
}
