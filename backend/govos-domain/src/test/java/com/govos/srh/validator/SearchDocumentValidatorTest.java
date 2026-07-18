package com.govos.srh.validator;

import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.exception.SearchDocumentException;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchDocumentValidatorTest {

    @Mock private SearchIndexRepository searchIndexRepository;

    private SearchDocumentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SearchDocumentValidator(searchIndexRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));

        assertThatCode(() -> validator.validateCreate(SrhTestFixtures.documentCreateRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingReferenceId() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        SearchDocumentCreateRequest request = new SearchDocumentCreateRequest(
                null, SrhTestFixtures.INDEX_ID, UUID.randomUUID(), SrhTestFixtures.ENTITY_TYPE,
                null, null, SrhTestFixtures.ORG_ID, null, null, 0L, null, null,
                null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchDocumentException.class);
    }

    @Test
    void shouldRejectMissingOrganizationId() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        SearchDocumentCreateRequest request = new SearchDocumentCreateRequest(
                null, SrhTestFixtures.INDEX_ID, UUID.randomUUID(), SrhTestFixtures.ENTITY_TYPE,
                SrhTestFixtures.REFERENCE_ID, null, null, null, null, 0L, null, null,
                null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchDocumentException.class);
    }

    @Test
    void shouldRejectInvalidMetadataMappingVersion() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        SearchDocumentCreateRequest request = new SearchDocumentCreateRequest(
                null, SrhTestFixtures.INDEX_ID, UUID.randomUUID(), SrhTestFixtures.ENTITY_TYPE,
                SrhTestFixtures.REFERENCE_ID, null, SrhTestFixtures.ORG_ID, null, null, 0L, null, null,
                SrhTestFixtures.ORG_ID, SrhTestFixtures.ENTITY_TYPE, SrhTestFixtures.REFERENCE_ID, null, 0,
                null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchDocumentException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        assertThatCode(() -> validator.validateUpdate(SrhTestFixtures.documentUpdateRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingEntityType() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        SearchDocumentCreateRequest request = new SearchDocumentCreateRequest(
                null, SrhTestFixtures.INDEX_ID, UUID.randomUUID(), " ",
                SrhTestFixtures.REFERENCE_ID, null, SrhTestFixtures.ORG_ID, null, null, 0L, null, null,
                null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchDocumentException.class);
    }

    @Test
    void shouldRejectInvalidMetadataEntityTypeLength() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        String longType = "x".repeat(101);
        SearchDocumentCreateRequest request = new SearchDocumentCreateRequest(
                null, SrhTestFixtures.INDEX_ID, UUID.randomUUID(), SrhTestFixtures.ENTITY_TYPE,
                SrhTestFixtures.REFERENCE_ID, null, SrhTestFixtures.ORG_ID, null, null, 0L, null, null,
                SrhTestFixtures.ORG_ID, longType, SrhTestFixtures.REFERENCE_ID, null, 1, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchDocumentException.class);
    }

    @Test
    void shouldRejectInvalidMetadataReferenceCodeLength() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        String longCode = "x".repeat(101);
        SearchDocumentCreateRequest request = new SearchDocumentCreateRequest(
                null, SrhTestFixtures.INDEX_ID, UUID.randomUUID(), SrhTestFixtures.ENTITY_TYPE,
                SrhTestFixtures.REFERENCE_ID, null, SrhTestFixtures.ORG_ID, null, null, 0L, null, null,
                SrhTestFixtures.ORG_ID, SrhTestFixtures.ENTITY_TYPE, SrhTestFixtures.REFERENCE_ID, longCode, 1,
                null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchDocumentException.class);
    }

    @Test
    void shouldRejectMissingSearchIndex() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(SrhTestFixtures.documentCreateRequest()))
                .isInstanceOf(SearchIndexNotFoundException.class);
    }
}
