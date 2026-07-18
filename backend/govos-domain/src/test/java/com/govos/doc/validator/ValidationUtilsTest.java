package com.govos.doc.validator;

import com.govos.doc.exception.ValidationResult;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilsTest {

    @Test
    void shouldValidateSha256Checksum() {
        ValidationResult valid = new ValidationResult();
        ValidationUtils.requireSha256Checksum(valid, "checksum", DocumentTestFixtures.SHA256);
        assertThat(valid.isValid()).isTrue();

        ValidationResult invalid = new ValidationResult();
        ValidationUtils.requireSha256Checksum(invalid, "checksum", "not-a-hash");
        assertThat(invalid.isValid()).isFalse();
    }

    @Test
    void shouldValidateMimeTypeAndDocumentNumber() {
        ValidationResult mime = new ValidationResult();
        ValidationUtils.requireMimeType(mime, "mimeType", "application/pdf");
        assertThat(mime.isValid()).isTrue();

        ValidationResult badMime = new ValidationResult();
        ValidationUtils.requireMimeType(badMime, "mimeType", "invalid");
        assertThat(badMime.isValid()).isFalse();

        ValidationResult docNum = new ValidationResult();
        ValidationUtils.requireDocumentNumber(docNum, "documentNumber", "DOC-001");
        assertThat(docNum.isValid()).isTrue();

        ValidationResult badDocNum = new ValidationResult();
        ValidationUtils.requireDocumentNumber(badDocNum, "documentNumber", "bad chars!");
        assertThat(badDocNum.isValid()).isFalse();
    }

    @Test
    void shouldValidateNumericAndTemporalHelpers() {
        ValidationResult positive = new ValidationResult();
        ValidationUtils.requirePositive(positive, "size", 1L);
        assertThat(positive.isValid()).isTrue();

        ValidationResult nonPositive = new ValidationResult();
        ValidationUtils.requirePositive(nonPositive, "size", 0L);
        assertThat(nonPositive.isValid()).isFalse();

        ValidationResult nonNegative = new ValidationResult();
        ValidationUtils.requireNonNegative(nonNegative, "days", 0);
        assertThat(nonNegative.isValid()).isTrue();

        ValidationResult future = new ValidationResult();
        ValidationUtils.requireFutureInstant(future, "expiresAt", Instant.parse("2099-01-01T00:00:00Z"));
        assertThat(future.isValid()).isTrue();

        ValidationResult past = new ValidationResult();
        ValidationUtils.requireFutureInstant(past, "expiresAt", Instant.parse("2000-01-01T00:00:00Z"));
        assertThat(past.isValid()).isFalse();
    }

    @Test
    void shouldValidateUuidAndEnum() {
        ValidationResult uuid = new ValidationResult();
        ValidationUtils.requireUuid(uuid, "id", UUID.randomUUID());
        assertThat(uuid.isValid()).isTrue();

        ValidationResult missing = new ValidationResult();
        ValidationUtils.requireUuid(missing, "id", null);
        assertThat(missing.isValid()).isFalse();

        ValidationResult enumResult = new ValidationResult();
        ValidationUtils.requireEnum(enumResult, "status", com.govos.doc.enums.DocumentStatus.UPLOADED);
        assertThat(enumResult.isValid()).isTrue();
    }

    @Test
    void shouldValidateTextMaxLengthAndNotNull() {
        ValidationResult text = new ValidationResult();
        ValidationUtils.requireText(text, "name", "value");
        assertThat(text.isValid()).isTrue();

        ValidationResult blank = new ValidationResult();
        ValidationUtils.requireText(blank, "name", " ");
        assertThat(blank.isValid()).isFalse();

        ValidationResult maxLen = new ValidationResult();
        ValidationUtils.requireMaxLength(maxLen, "name", "a".repeat(101), ValidationUtils.MAX_PROVIDER_NAME_LENGTH);
        assertThat(maxLen.isValid()).isFalse();

        ValidationResult notNull = new ValidationResult();
        ValidationUtils.requireNotNull(notNull, "field", null);
        assertThat(notNull.isValid()).isFalse();
    }

    @Test
    void shouldValidateBeanConstraintViolations() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        ValidationResult result = ValidationUtils.validateBean(
                jakartaValidator,
                DocumentTestFixtures.createStorageProviderRequest(),
                "request");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldHandleNullBeanValidationGracefully() {
        ValidationResult result = ValidationUtils.validateBean(null, null, "request");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldRejectNullAndNonPositiveNumbers() {
        ValidationResult nullPositive = new ValidationResult();
        ValidationUtils.requirePositive(nullPositive, "size", null);
        assertThat(nullPositive.isValid()).isFalse();

        ValidationResult nullNonNegative = new ValidationResult();
        ValidationUtils.requireNonNegative(nullNonNegative, "days", null);
        assertThat(nullNonNegative.isValid()).isFalse();

        ValidationResult negative = new ValidationResult();
        ValidationUtils.requireNonNegative(negative, "days", -1);
        assertThat(negative.isValid()).isFalse();
    }

    @Test
    void shouldRejectEmptySha256Checksum() {
        ValidationResult result = new ValidationResult();
        ValidationUtils.requireSha256Checksum(result, "checksum", " ");
        assertThat(result.isValid()).isFalse();
    }
}
