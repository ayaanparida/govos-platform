package com.govos.doc.validator;

import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.dto.folder.UpdateFolderRequest;
import com.govos.doc.exception.ValidationResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FolderValidator {

    private final Validator jakartaValidator;

    public FolderValidator(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    public void validateCreate(CreateFolderRequest request) {
        validateCreateResult(request).throwIfInvalid();
    }

    public ValidationResult validateCreateResult(CreateFolderRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "createFolderRequest");
        if (request == null) {
            result.addError("createFolderRequest", "Create folder request is required");
            return result;
        }
        ValidationUtils.requireUuid(result, "organizationId", request.organizationId());
        ValidationUtils.requireUuid(result, "ownerId", request.ownerId());
        ValidationUtils.requireText(result, "name", request.name());
        ValidationUtils.requireMaxLength(result, "name", request.name(), ValidationUtils.MAX_FOLDER_NAME_LENGTH);
        validatePath(result, request.materializedPath(), request.depthLevel());
        validateParentNotSelf(result, null, request.parentFolderId());
        return result;
    }

    public void validateUpdate(UpdateFolderRequest request, UUID folderId) {
        validateUpdateResult(request, folderId).throwIfInvalid();
    }

    public ValidationResult validateUpdateResult(UpdateFolderRequest request, UUID folderId) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "updateFolderRequest");
        if (request == null) {
            result.addError("updateFolderRequest", "Update folder request is required");
            return result;
        }
        ValidationUtils.requireNotNull(result, "version", request.version());
        if (request.name() != null) {
            ValidationUtils.requireText(result, "name", request.name());
            ValidationUtils.requireMaxLength(result, "name", request.name(), ValidationUtils.MAX_FOLDER_NAME_LENGTH);
        }
        validatePath(result, request.materializedPath(), request.depthLevel());
        validateParentNotSelf(result, folderId, request.parentFolderId());
        return result;
    }

    public void validateDelete(UUID folderId) {
        validateDeleteResult(folderId).throwIfInvalid();
    }

    public ValidationResult validateDeleteResult(UUID folderId) {
        ValidationResult result = new ValidationResult();
        ValidationUtils.requireUuid(result, "folderId", folderId);
        return result;
    }

    private void validatePath(ValidationResult result, String path, Integer depthLevel) {
        if (path != null) {
            ValidationUtils.requireMaxLength(result, "materializedPath", path, ValidationUtils.MAX_PATH_LENGTH);
        }
        if (depthLevel != null && depthLevel > ValidationUtils.MAX_FOLDER_DEPTH) {
            result.addError(
                    "depthLevel",
                    "Folder hierarchy depth must not exceed " + ValidationUtils.MAX_FOLDER_DEPTH,
                    "DOC_FOLDER_DEPTH_EXCEEDED");
        }
    }

    private void validateParentNotSelf(ValidationResult result, UUID folderId, UUID parentFolderId) {
        if (folderId != null && parentFolderId != null && folderId.equals(parentFolderId)) {
            result.addError("parentFolderId", "Folder cannot be its own parent", "DOC_FOLDER_CYCLE");
        }
    }
}
