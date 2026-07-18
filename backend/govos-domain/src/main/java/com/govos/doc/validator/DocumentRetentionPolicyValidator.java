package com.govos.doc.validator;

import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import com.govos.doc.exception.ValidationResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class DocumentRetentionPolicyValidator {

    private final Validator jakartaValidator;

    public DocumentRetentionPolicyValidator(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    public void validateCreate(CreateRetentionPolicyRequest request) {
        validateCreateResult(request).throwIfInvalid();
    }

    public ValidationResult validateCreateResult(CreateRetentionPolicyRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "createRetentionPolicyRequest");
        if (request == null) {
            result.addError("createRetentionPolicyRequest", "Create retention policy request is required");
            return result;
        }
        ValidationUtils.requireText(result, "name", request.name());
        ValidationUtils.requireMaxLength(result, "name", request.name(), ValidationUtils.MAX_POLICY_NAME_LENGTH);
        ValidationUtils.requireNonNegative(result, "retentionDays", request.retentionDays());
        ValidationUtils.requireEnum(result, "actionOnExpiry", request.actionOnExpiry());
        validateLegalHold(result, request.legalHold(), request.retentionDays());
        return result;
    }

    public void validateUpdate(UpdateRetentionPolicyRequest request) {
        validateUpdateResult(request).throwIfInvalid();
    }

    public ValidationResult validateUpdateResult(UpdateRetentionPolicyRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "updateRetentionPolicyRequest");
        if (request == null) {
            result.addError("updateRetentionPolicyRequest", "Update retention policy request is required");
            return result;
        }
        ValidationUtils.requireNotNull(result, "version", request.version());
        if (request.name() != null) {
            ValidationUtils.requireText(result, "name", request.name());
            ValidationUtils.requireMaxLength(result, "name", request.name(), ValidationUtils.MAX_POLICY_NAME_LENGTH);
        }
        if (request.retentionDays() != null) {
            ValidationUtils.requireNonNegative(result, "retentionDays", request.retentionDays());
        }
        if (request.actionOnExpiry() != null) {
            ValidationUtils.requireEnum(result, "actionOnExpiry", request.actionOnExpiry());
        }
        validateLegalHold(result, request.legalHold(), request.retentionDays());
        return result;
    }

    private void validateLegalHold(ValidationResult result, Boolean legalHold, Integer retentionDays) {
        if (Boolean.TRUE.equals(legalHold) && retentionDays != null && retentionDays <= 0) {
            result.addError(
                    "legalHold",
                    "Legal hold requires a positive retentionDays value when retention period is specified",
                    "DOC_RETENTION_LEGAL_HOLD");
        }
    }
}
