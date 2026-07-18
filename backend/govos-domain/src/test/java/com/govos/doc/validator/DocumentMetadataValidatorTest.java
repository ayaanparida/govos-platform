package com.govos.doc.validator;

import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentMetadataValidatorTest {

    private DocumentMetadataValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new DocumentMetadataValidator(jakartaValidator);
    }

    @Test
    void shouldValidateDocumentScopeWhenDocumentIdPresent() {
        assertThatCode(() -> validator.validateDocumentScope(DocumentTestFixtures.DOCUMENT_ID, null))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDocumentScopeWhenDocumentIdMissing() {
        assertThatThrownBy(() -> validator.validateDocumentScope(null, DocumentTestFixtures.VERSION_ID))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        assertThatCode(() -> validator.validateUpdate(DocumentTestFixtures.updateMetadataRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUpdateWhenVersionMissing() {
        UpdateDocumentMetadataRequest request = new UpdateDocumentMetadataRequest(
                null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(DocumentValidationException.class);
    }
}
