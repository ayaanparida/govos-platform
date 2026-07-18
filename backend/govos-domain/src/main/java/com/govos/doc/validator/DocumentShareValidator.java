package com.govos.doc.validator;

import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.enums.ShareType;
import com.govos.doc.exception.ValidationResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DocumentShareValidator {

    private final Validator jakartaValidator;

    public DocumentShareValidator(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    public void validateShare(CreateShareRequest request) {
        validateCreate(request);
    }

    public void validateCreate(CreateShareRequest request) {
        validateCreateResult(request).throwIfInvalid();
    }

    public ValidationResult validateCreateResult(CreateShareRequest request) {
        ValidationResult result = ValidationUtils.validateBean(jakartaValidator, request, "createShareRequest");
        if (request == null) {
            result.addError("createShareRequest", "Create share request is required");
            return result;
        }
        ValidationUtils.requireUuid(result, "documentId", request.documentId());
        ValidationUtils.requireEnum(result, "shareType", request.shareType());
        ValidationUtils.requireUuid(result, "createdById", request.createdById());
        ValidationUtils.requireText(result, "permission", request.permission());
        if (request.expiresAt() != null) {
            ValidationUtils.requireFutureInstant(result, "expiresAt", request.expiresAt());
        }
        if (result.isValid()) {
            validateShareTypeRules(result, request);
        }
        return result;
    }

    private void validateShareTypeRules(ValidationResult result, CreateShareRequest request) {
        ShareType shareType = request.shareType();
        switch (shareType) {
            case USER -> {
                if (request.sharedWithUserId() == null && !StringUtils.hasText(request.sharedWithEmail())) {
                    result.addError(
                            "sharedWithUserId",
                            "USER share requires sharedWithUserId or sharedWithEmail",
                            "DOC_SHARE_RECIPIENT_REQUIRED");
                }
            }
            case ROLE -> {
                if (request.sharedWithRoleId() == null) {
                    result.addError(
                            "sharedWithRoleId",
                            "ROLE share requires sharedWithRoleId",
                            "DOC_SHARE_ROLE_REQUIRED");
                }
            }
            case PUBLIC_LINK -> {
                if (!StringUtils.hasText(request.publicLinkUrl())) {
                    result.addError(
                            "publicLinkUrl",
                            "PUBLIC_LINK share requires publicLinkUrl",
                            "DOC_SHARE_PUBLIC_LINK_REQUIRED");
                }
            }
            case SIGNED_URL -> {
                if (request.signedUrlExpiresAt() == null) {
                    result.addError(
                            "signedUrlExpiresAt",
                            "SIGNED_URL share requires signedUrlExpiresAt",
                            "DOC_SHARE_SIGNED_URL_REQUIRED");
                } else {
                    ValidationUtils.requireFutureInstant(result, "signedUrlExpiresAt", request.signedUrlExpiresAt());
                }
            }
            default -> result.addError("shareType", "Unsupported share type: " + shareType);
        }
    }
}
