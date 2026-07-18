package com.govos.doc.validator;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentCategoryValidatorTest {

    private DocumentCategoryValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new DocumentCategoryValidator(jakartaValidator);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        assertThatCode(() -> validator.validateCreate(DocumentTestFixtures.createCategoryRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenCodeMissing() {
        CreateDocumentCategoryRequest request = new CreateDocumentCategoryRequest(
                " ", "General", DocumentTestFixtures.ORG_ID, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenNameMissing() {
        CreateDocumentCategoryRequest request = new CreateDocumentCategoryRequest(
                "CAT-001", " ", DocumentTestFixtures.ORG_ID, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectUpdateWhenParentIsSelf() {
        UpdateDocumentCategoryRequest request = new UpdateDocumentCategoryRequest(
                null, DocumentTestFixtures.CATEGORY_ID, null, null, null, null, 0L);

        assertThatThrownBy(() -> validator.validateUpdate(request, DocumentTestFixtures.CATEGORY_ID))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        UpdateDocumentCategoryRequest request = new UpdateDocumentCategoryRequest(
                "Updated", null, null, null, null, true, 0L);

        assertThatCode(() -> validator.validateUpdate(request, DocumentTestFixtures.CATEGORY_ID))
                .doesNotThrowAnyException();
    }
}
