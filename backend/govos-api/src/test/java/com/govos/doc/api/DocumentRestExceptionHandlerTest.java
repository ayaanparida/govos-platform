package com.govos.doc.api;

import com.govos.doc.api.advice.DocumentRestExceptionHandler;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.ValidationResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentRestExceptionHandlerTest {

    private final DocumentRestExceptionHandler handler = new DocumentRestExceptionHandler();

    @Test
    void shouldMapDocumentNotFoundTo404() {
        var response = handler.handleNotFound(
                new DocumentNotFoundException(UUID.randomUUID()),
                new MockHttpServletRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
    }

    @Test
    void shouldMapDuplicateValidationTo409() {
        ValidationResult result = new ValidationResult();
        result.addError("code", "Duplicate", "DOC_DUPLICATE_CATEGORY_CODE");

        var response = handler.handleDocumentValidation(
                new DocumentValidationException(result),
                new MockHttpServletRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("CONFLICT");
    }

    @Test
    void shouldMapValidationErrorTo422() {
        var response = handler.handleDocumentValidation(
                new DocumentValidationException("Invalid field value"),
                new MockHttpServletRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("DOCUMENT_VALIDATION_ERROR");
    }
}
