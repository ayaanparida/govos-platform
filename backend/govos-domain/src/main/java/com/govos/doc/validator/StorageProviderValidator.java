package com.govos.doc.validator;

import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.storage.UpdateStorageProviderRequest;
import com.govos.doc.exception.ValidationResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class StorageProviderValidator {

    private final Validator jakartaValidator;

    public StorageProviderValidator(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    public void validateCreate(CreateStorageProviderRequest request) {
        validateCreateResult(request).throwIfInvalid();
    }

    public ValidationResult validateCreateResult(CreateStorageProviderRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "createStorageProviderRequest");
        if (request == null) {
            result.addError("createStorageProviderRequest", "Create storage provider request is required");
            return result;
        }
        ValidationUtils.requireText(result, "providerName", request.providerName());
        ValidationUtils.requireMaxLength(
                result, "providerName", request.providerName(), ValidationUtils.MAX_PROVIDER_NAME_LENGTH);
        ValidationUtils.requireEnum(result, "providerType", request.providerType());
        ValidationUtils.requireText(result, "bucketName", request.bucketName());
        validateDefaultProviderRules(result, request.isDefault(), request.providerName(), request.bucketName());
        return result;
    }

    public void validateUpdate(UpdateStorageProviderRequest request) {
        validateUpdateResult(request).throwIfInvalid();
    }

    public ValidationResult validateUpdateResult(UpdateStorageProviderRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "updateStorageProviderRequest");
        if (request == null) {
            result.addError("updateStorageProviderRequest", "Update storage provider request is required");
            return result;
        }
        ValidationUtils.requireNotNull(result, "version", request.version());
        if (request.providerName() != null) {
            ValidationUtils.requireText(result, "providerName", request.providerName());
            ValidationUtils.requireMaxLength(
                    result, "providerName", request.providerName(), ValidationUtils.MAX_PROVIDER_NAME_LENGTH);
        }
        if (request.providerType() != null) {
            ValidationUtils.requireEnum(result, "providerType", request.providerType());
        }
        if (request.bucketName() != null) {
            ValidationUtils.requireText(result, "bucketName", request.bucketName());
        }
        validateDefaultProviderRules(result, request.isDefault(), request.providerName(), request.bucketName());
        return result;
    }

    private void validateDefaultProviderRules(
            ValidationResult result,
            Boolean isDefault,
            String providerName,
            String bucketName) {
        if (!Boolean.TRUE.equals(isDefault)) {
            return;
        }
        if (!result.isValid()) {
            return;
        }
        if (!org.springframework.util.StringUtils.hasText(providerName)) {
            result.addError(
                    "providerName",
                    "Default storage provider requires providerName",
                    "DOC_STORAGE_DEFAULT_NAME_REQUIRED");
        }
        if (!org.springframework.util.StringUtils.hasText(bucketName)) {
            result.addError(
                    "bucketName",
                    "Default storage provider requires bucketName",
                    "DOC_STORAGE_DEFAULT_BUCKET_REQUIRED");
        }
    }
}
