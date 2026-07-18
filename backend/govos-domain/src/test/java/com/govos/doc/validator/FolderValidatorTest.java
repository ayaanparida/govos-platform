package com.govos.doc.validator;

import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.dto.folder.UpdateFolderRequest;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.support.DocumentTestFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FolderValidatorTest {

    private FolderValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new FolderValidator(jakartaValidator);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        assertThatCode(() -> validator.validateCreate(DocumentTestFixtures.createFolderRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenNameMissing() {
        CreateFolderRequest request = new CreateFolderRequest(
                " ", DocumentTestFixtures.ORG_ID, DocumentTestFixtures.OWNER_ID,
                null, "/Folder", 0, "FLD-001", true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenDepthExceeded() {
        CreateFolderRequest request = new CreateFolderRequest(
                "Deep", DocumentTestFixtures.ORG_ID, DocumentTestFixtures.OWNER_ID,
                null, "/Deep", ValidationUtils.MAX_FOLDER_DEPTH + 1, "FLD-001", true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenParentIsSelf() {
        CreateFolderRequest request = new CreateFolderRequest(
                "Folder", DocumentTestFixtures.ORG_ID, DocumentTestFixtures.OWNER_ID,
                DocumentTestFixtures.FOLDER_ID, "/Folder", 0, "FLD-001", true);

        assertThatThrownBy(() -> validator.validateUpdate(
                new UpdateFolderRequest(null, DocumentTestFixtures.FOLDER_ID, null, null, null, 0L),
                DocumentTestFixtures.FOLDER_ID))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        UpdateFolderRequest request = new UpdateFolderRequest(
                "Renamed", null, "/Renamed", 0, true, 0L);

        assertThatCode(() -> validator.validateUpdate(request, DocumentTestFixtures.FOLDER_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldValidateDeleteWhenFolderIdPresent() {
        assertThatCode(() -> validator.validateDelete(DocumentTestFixtures.FOLDER_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDeleteWhenFolderIdMissing() {
        assertThatThrownBy(() -> validator.validateDelete(null))
                .isInstanceOf(DocumentValidationException.class);
    }
}
