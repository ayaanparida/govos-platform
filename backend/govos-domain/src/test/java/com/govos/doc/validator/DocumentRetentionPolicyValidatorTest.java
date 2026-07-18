package com.govos.doc.validator;

import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import com.govos.doc.enums.RetentionAction;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentRetentionPolicyValidatorTest {

    private DocumentRetentionPolicyValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new DocumentRetentionPolicyValidator(jakartaValidator);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        assertThatCode(() -> validator.validateCreate(DocumentTestFixtures.createRetentionPolicyRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenNameMissing() {
        CreateRetentionPolicyRequest request = new CreateRetentionPolicyRequest(
                "POL-001", " ", DocumentTestFixtures.ORG_ID, 365, RetentionAction.ARCHIVE, false, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenLegalHoldWithoutRetentionDays() {
        CreateRetentionPolicyRequest request = new CreateRetentionPolicyRequest(
                "POL-001", "Hold", DocumentTestFixtures.ORG_ID, 0, RetentionAction.ARCHIVE, true, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        UpdateRetentionPolicyRequest request = new UpdateRetentionPolicyRequest(
                "Updated", 180, RetentionAction.DELETE, false, "Desc", true, 0L);

        assertThatCode(() -> validator.validateUpdate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUpdateWhenVersionMissing() {
        UpdateRetentionPolicyRequest request = new UpdateRetentionPolicyRequest(
                "Updated", 180, RetentionAction.DELETE, false, null, true, null);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(DocumentValidationException.class);
    }
}
