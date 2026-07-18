package com.govos.srh.validator;

import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.exception.SearchIndexAlreadyExistsException;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.exception.SearchIndexValidationException;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchIndexValidatorTest {

    @Mock private SearchIndexRepository searchIndexRepository;

    private SearchIndexValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SearchIndexValidator(searchIndexRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        stubNoDuplicateCodeOrName();

        assertThatCode(() -> validator.validateCreate(SrhTestFixtures.indexCreateRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDuplicateCode() {
        when(searchIndexRepository.existsByCodeAndDeletedFalse(SrhTestFixtures.INDEX_CODE)).thenReturn(true);

        assertThatThrownBy(() -> validator.validateCreate(SrhTestFixtures.indexCreateRequest()))
                .isInstanceOf(SearchIndexAlreadyExistsException.class);
    }

    @Test
    void shouldRejectDuplicateName() {
        SearchIndex existing = SrhTestFixtures.searchIndex(UUID.randomUUID());
        existing.setName("Complaint Search Index");
        when(searchIndexRepository.findAllByStatusAndDeletedFalse(SearchIndexStatus.ACTIVE))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> validator.validateCreate(SrhTestFixtures.indexCreateRequest()))
                .isInstanceOf(SearchIndexAlreadyExistsException.class);
    }

    @Test
    void shouldRejectInvalidMappingVersion() {
        SearchIndexCreateRequest request = new SearchIndexCreateRequest(
                SrhTestFixtures.INDEX_CODE, "Name", null, SearchEngineType.OPENSEARCH, 0, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchIndexValidationException.class);
    }

    @Test
    void shouldRejectMissingEngineType() {
        SearchIndexCreateRequest request = new SearchIndexCreateRequest(
                SrhTestFixtures.INDEX_CODE, "Name", null, null, 1, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchIndexValidationException.class);
    }

    @Test
    void shouldRejectDeletedEntity() {
        SearchIndex deleted = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        deleted.setDeleted(true);

        assertThatThrownBy(() -> validator.validateNotDeleted(deleted))
                .isInstanceOf(SearchIndexValidationException.class);
    }

    @Test
    void shouldRequireExists() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(index));

        assertThat(validator.requireExists(SrhTestFixtures.INDEX_ID)).isEqualTo(index);
    }

    @Test
    void shouldThrowWhenRequireExistsNotFound() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.requireExists(SrhTestFixtures.INDEX_ID))
                .isInstanceOf(SearchIndexNotFoundException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        assertThatCode(() -> validator.validateUpdate(SrhTestFixtures.indexUpdateRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldValidateCodeUniquenessWithExcludeId() {
        when(searchIndexRepository.findByCodeAndDeletedFalse(SrhTestFixtures.INDEX_CODE))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));

        assertThatCode(() -> validator.validateCodeUniqueness(SrhTestFixtures.INDEX_CODE, SrhTestFixtures.INDEX_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCodeUniquenessWithDifferentExcludeId() {
        UUID otherId = UUID.randomUUID();
        when(searchIndexRepository.findByCodeAndDeletedFalse(SrhTestFixtures.INDEX_CODE))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(otherId)));

        assertThatThrownBy(() -> validator.validateCodeUniqueness(SrhTestFixtures.INDEX_CODE, SrhTestFixtures.INDEX_ID))
                .isInstanceOf(SearchIndexAlreadyExistsException.class);
    }

    @Test
    void shouldValidateNameUniquenessWithExcludeId() {
        SearchIndex existing = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        existing.setName("Complaint Search Index");
        when(searchIndexRepository.findAllByStatusAndDeletedFalse(SearchIndexStatus.ACTIVE))
                .thenReturn(List.of(existing));
        for (SearchIndexStatus status : SearchIndexStatus.values()) {
            if (status != SearchIndexStatus.ACTIVE) {
                when(searchIndexRepository.findAllByStatusAndDeletedFalse(status)).thenReturn(List.of());
            }
        }

        assertThatCode(() -> validator.validateNameUniqueness("Complaint Search Index", SrhTestFixtures.INDEX_ID))
                .doesNotThrowAnyException();
    }

    private void stubNoDuplicateCodeOrName() {
        when(searchIndexRepository.existsByCodeAndDeletedFalse(SrhTestFixtures.INDEX_CODE)).thenReturn(false);
        for (SearchIndexStatus status : SearchIndexStatus.values()) {
            when(searchIndexRepository.findAllByStatusAndDeletedFalse(status)).thenReturn(List.of());
        }
    }
}
