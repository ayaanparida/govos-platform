package com.govos.doc.validator;

import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.enums.ShareType;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentShareValidatorTest {

    private DocumentShareValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new DocumentShareValidator(jakartaValidator);
    }

    @Test
    void shouldValidateCreateWhenUserShareValid() {
        assertThatCode(() -> validator.validateCreate(
                DocumentTestFixtures.createShareRequest(DocumentTestFixtures.DOCUMENT_ID)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenDocumentIdMissing() {
        CreateShareRequest request = new CreateShareRequest(
                null, ShareType.USER, DocumentTestFixtures.USER_ID, null, null,
                DocumentTestFixtures.OWNER_ID, Instant.parse("2099-01-01T00:00:00Z"), "READ", null, null);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectRoleShareWhenRoleIdMissing() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.ROLE, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", null, null);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectPublicLinkShareWhenUrlMissing() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.PUBLIC_LINK, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", null, null);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectSignedUrlShareWhenExpiryMissing() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.SIGNED_URL, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", null, null);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateSignedUrlShareWhenExpiryProvided() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.SIGNED_URL, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", null, Instant.parse("2099-01-01T00:00:00Z"));

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenExpiresAtInPast() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.USER, DocumentTestFixtures.USER_ID, null, null,
                DocumentTestFixtures.OWNER_ID, Instant.parse("2000-01-01T00:00:00Z"), "READ", null, null);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateUserShareWithEmailOnly() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.USER, null, null, "user@example.com",
                DocumentTestFixtures.OWNER_ID, null, "READ", null, null);

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSignedUrlShareWhenExpiryInPast() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.SIGNED_URL, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", null, Instant.parse("2000-01-01T00:00:00Z"));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldDelegateValidateShareToValidateCreate() {
        CreateShareRequest request = DocumentTestFixtures.createShareRequest(DocumentTestFixtures.DOCUMENT_ID);

        assertThatCode(() -> validator.validateShare(request)).doesNotThrowAnyException();
    }
}
