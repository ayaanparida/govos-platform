package com.govos.doc.validator;

import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.storage.UpdateStorageProviderRequest;
import com.govos.doc.enums.StorageProviderType;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageProviderValidatorTest {

    private StorageProviderValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new StorageProviderValidator(jakartaValidator);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        assertThatCode(() -> validator.validateCreate(DocumentTestFixtures.createStorageProviderRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenProviderNameMissing() {
        CreateStorageProviderRequest request = new CreateStorageProviderRequest(
                "SP-001", " ", StorageProviderType.MINIO, "bucket", "http://localhost:9000",
                "local", true, false, "secret", true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenBucketNameMissing() {
        CreateStorageProviderRequest request = new CreateStorageProviderRequest(
                "SP-001", "provider", StorageProviderType.MINIO, " ", "http://localhost:9000",
                "local", true, false, "secret", true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                "updated", StorageProviderType.MINIO, "bucket", "http://localhost:9000",
                "local", true, false, "secret", true, 0L);

        assertThatCode(() -> validator.validateUpdate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDefaultProviderWhenNameMissing() {
        CreateStorageProviderRequest request = new CreateStorageProviderRequest(
                "SP-001", " ", StorageProviderType.MINIO, "bucket", "http://localhost:9000",
                "local", true, true, "secret", true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectUpdateWhenVersionMissing() {
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                "updated", null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectDefaultProviderWhenBucketMissing() {
        CreateStorageProviderRequest request = new CreateStorageProviderRequest(
                "SP-001", "provider", StorageProviderType.MINIO, " ", "http://localhost:9000",
                "local", true, true, "secret", true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectProviderNameExceedingMaxLength() {
        String longName = "a".repeat(101);
        CreateStorageProviderRequest request = new CreateStorageProviderRequest(
                "SP-001", longName, StorageProviderType.MINIO, "bucket", "http://localhost:9000",
                "local", true, false, "secret", true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectUpdateDefaultProviderWhenBucketBlank() {
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                null, null, " ", null, null, null, true, null, null, 0L);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectUpdateDefaultProviderWhenProviderNameNull() {
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                null, null, "bucket", null, null, null, true, null, null, 0L);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldReturnInvalidResultWhenCreateRequestNull() {
        ValidationResult result = validator.validateCreateResult(null);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldReturnInvalidResultWhenUpdateRequestNull() {
        ValidationResult result = validator.validateUpdateResult(null);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldValidateCreateResultWithoutThrowingWhenValid() {
        ValidationResult result = validator.validateCreateResult(
                DocumentTestFixtures.createStorageProviderRequest());

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldRejectUpdateWhenBucketNameBlank() {
        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                null, null, " ", null, null, null, null, null, null, 0L);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldSkipDefaultRulesWhenIsDefaultFalse() {
        CreateStorageProviderRequest request = new CreateStorageProviderRequest(
                "SP-001", "provider", StorageProviderType.MINIO, "bucket", "http://localhost:9000",
                "local", true, false, "secret", true);

        ValidationResult result = validator.validateCreateResult(request);

        assertThat(result.isValid()).isTrue();
    }
}
