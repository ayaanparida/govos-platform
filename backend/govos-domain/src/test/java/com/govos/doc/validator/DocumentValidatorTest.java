package com.govos.doc.validator;

import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentValidatorTest {

    private DocumentValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new DocumentValidator(jakartaValidator);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        assertThatCode(() -> validator.validateCreate(DocumentTestFixtures.createDocumentRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenTitleMissing() {
        CreateDocumentRequest request = new CreateDocumentRequest(
                " ", "Description", DocumentTestFixtures.ORG_ID, DocumentTestFixtures.OWNER_ID,
                DocumentClassification.INTERNAL, null, null, null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenOrganizationMissing() {
        CreateDocumentRequest request = new CreateDocumentRequest(
                "Title", "Description", null, DocumentTestFixtures.OWNER_ID,
                DocumentClassification.INTERNAL, null, null, null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        assertThatCode(() -> validator.validateUpdate(DocumentTestFixtures.updateDocumentRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUpdateWhenVersionMissing() {
        UpdateDocumentRequest request = new UpdateDocumentRequest(
                "Updated", null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldAllowValidStatusTransition() {
        assertThatCode(() -> validator.validateStatusTransition(DocumentStatus.UPLOADED, DocumentStatus.PROCESSING))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        assertThatThrownBy(() -> validator.validateStatusTransition(DocumentStatus.UPLOADED, DocumentStatus.READY))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateDeleteWhenDocumentIdPresent() {
        assertThatCode(() -> validator.validateDelete(DocumentTestFixtures.DOCUMENT_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDeleteWhenDocumentIdMissing() {
        assertThatThrownBy(() -> validator.validateDelete(null))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateRestoreWhenStatusDeleted() {
        assertThatCode(() -> validator.validateRestore(DocumentStatus.DELETED))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectRestoreWhenStatusNotDeleted() {
        assertThatThrownBy(() -> validator.validateRestore(DocumentStatus.UPLOADED))
                .isInstanceOf(DocumentValidationException.class);
    }
}
