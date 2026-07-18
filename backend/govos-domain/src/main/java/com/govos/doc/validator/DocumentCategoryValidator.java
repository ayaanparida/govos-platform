package com.govos.doc.validator;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
import com.govos.doc.exception.ValidationResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentCategoryValidator {

    private final Validator jakartaValidator;

    public DocumentCategoryValidator(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    public void validateCreate(CreateDocumentCategoryRequest request) {
        validateCreateResult(request).throwIfInvalid();
    }

    public ValidationResult validateCreateResult(CreateDocumentCategoryRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "createDocumentCategoryRequest");
        if (request == null) {
            result.addError("createDocumentCategoryRequest", "Create document category request is required");
            return result;
        }
        ValidationUtils.requireText(result, "code", request.code());
        ValidationUtils.requireMaxLength(result, "code", request.code(), ValidationUtils.MAX_CATEGORY_CODE_LENGTH);
        ValidationUtils.requireText(result, "name", request.name());
        ValidationUtils.requireMaxLength(result, "name", request.name(), ValidationUtils.MAX_CATEGORY_NAME_LENGTH);
        validateParentNotSelf(result, null, request.parentCategoryId());
        return result;
    }

    public void validateUpdate(UpdateDocumentCategoryRequest request, UUID categoryId) {
        validateUpdateResult(request, categoryId).throwIfInvalid();
    }

    public ValidationResult validateUpdateResult(UpdateDocumentCategoryRequest request, UUID categoryId) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "updateDocumentCategoryRequest");
        if (request == null) {
            result.addError("updateDocumentCategoryRequest", "Update document category request is required");
            return result;
        }
        ValidationUtils.requireNotNull(result, "version", request.version());
        if (request.name() != null) {
            ValidationUtils.requireText(result, "name", request.name());
            ValidationUtils.requireMaxLength(result, "name", request.name(), ValidationUtils.MAX_CATEGORY_NAME_LENGTH);
        }
        validateParentNotSelf(result, categoryId, request.parentCategoryId());
        return result;
    }

    private void validateParentNotSelf(ValidationResult result, UUID categoryId, UUID parentCategoryId) {
        if (categoryId != null && parentCategoryId != null && categoryId.equals(parentCategoryId)) {
            result.addError("parentCategoryId", "Category cannot be its own parent", "DOC_CATEGORY_CYCLE");
        }
    }
}
