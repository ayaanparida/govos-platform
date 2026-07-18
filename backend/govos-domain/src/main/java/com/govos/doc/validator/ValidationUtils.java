package com.govos.doc.validator;

import com.govos.doc.exception.ValidationResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;


public final class ValidationUtils {

    public static final int MAX_TITLE_LENGTH = 500;
    public static final int MAX_FOLDER_NAME_LENGTH = 255;
    public static final int MAX_PATH_LENGTH = 2048;
    public static final int MAX_FOLDER_DEPTH = 50;
    public static final int MAX_CATEGORY_CODE_LENGTH = 100;
    public static final int MAX_CATEGORY_NAME_LENGTH = 255;
    public static final int MAX_POLICY_NAME_LENGTH = 255;
    public static final int MAX_PROVIDER_NAME_LENGTH = 100;
    public static final int MAX_OCR_TEXT_LENGTH = 1_000_000;
    public static final int MAX_METADATA_JSON_LENGTH = 65_536;
    public static final int MAX_DOCUMENT_NUMBER_LENGTH = 100;
    public static final int MAX_FILENAME_LENGTH = 500;
    public static final int MAX_STORAGE_KEY_LENGTH = 1024;

    private static final Pattern SHA256_PATTERN = Pattern.compile("^[a-fA-F0-9]{64}$");
    private static final Pattern MIME_TYPE_PATTERN = Pattern.compile("^[a-zA-Z0-9!#$&\\-^_+.]+/[a-zA-Z0-9!#$&\\-^_+.]+$");
    private static final Pattern DOCUMENT_NUMBER_PATTERN = Pattern.compile("^[A-Za-z0-9._\\-/]+$");

    private ValidationUtils() {
    }

    public static ValidationResult validateBean(Validator jakartaValidator, Object bean, String objectName) {
        ValidationResult result = new ValidationResult();
        if (jakartaValidator == null || bean == null) {
            return result;
        }
        for (ConstraintViolation<Object> violation : jakartaValidator.validate(bean)) {
            result.addError(
                    objectName + "." + violation.getPropertyPath(),
                    violation.getMessage(),
                    violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
        }
        return result;
    }

    public static void requireNotNull(ValidationResult result, String field, Object value) {
        if (value == null) {
            result.addError(field, field + " is required");
        }
    }

    public static void requireText(ValidationResult result, String field, String value) {
        if (!StringUtils.hasText(value)) {
            result.addError(field, field + " is required");
        }
    }

    public static void requireMaxLength(ValidationResult result, String field, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            result.addError(field, field + " must not exceed " + maxLength + " characters");
        }
    }

    public static void requirePositive(ValidationResult result, String field, Number value) {
        if (value == null) {
            result.addError(field, field + " is required");
        } else if (value.longValue() <= 0) {
            result.addError(field, field + " must be greater than zero");
        }
    }

    public static void requireNonNegative(ValidationResult result, String field, Integer value) {
        if (value == null) {
            result.addError(field, field + " is required");
        } else if (value < 0) {
            result.addError(field, field + " must be zero or greater");
        }
    }

    public static void requireUuid(ValidationResult result, String field, UUID value) {
        requireNotNull(result, field, value);
    }

    public static void requireSha256Checksum(ValidationResult result, String field, String checksum) {
        requireText(result, field, checksum);
        if (StringUtils.hasText(checksum) && !SHA256_PATTERN.matcher(checksum).matches()) {
            result.addError(field, field + " must be a valid SHA-256 hex value");
        }
    }

    public static void requireMimeType(ValidationResult result, String field, String mimeType) {
        requireText(result, field, mimeType);
        if (StringUtils.hasText(mimeType) && !MIME_TYPE_PATTERN.matcher(mimeType).matches()) {
            result.addError(field, field + " must be a valid MIME type");
        }
    }

    public static void requireDocumentNumber(ValidationResult result, String field, String documentNumber) {
        if (!StringUtils.hasText(documentNumber)) {
            return;
        }
        requireMaxLength(result, field, documentNumber, MAX_DOCUMENT_NUMBER_LENGTH);
        if (StringUtils.hasText(documentNumber) && !DOCUMENT_NUMBER_PATTERN.matcher(documentNumber).matches()) {
            result.addError(field, field + " contains invalid characters");
        }
    }

    public static void requireFutureInstant(ValidationResult result, String field, Instant value) {
        if (value != null && !value.isAfter(Instant.now())) {
            result.addError(field, field + " must be in the future");
        }
    }

    public static void requireEnum(ValidationResult result, String field, Enum<?> value) {
        requireNotNull(result, field, value);
    }
}
