package com.govos.srh.validator;

import com.govos.srh.dto.SearchAliasCreateRequest;
import com.govos.srh.dto.SearchAliasUpdateRequest;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.exception.SearchAliasException;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.repository.SearchAliasRepository;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchAliasValidatorTest {

    @Mock private SearchIndexRepository searchIndexRepository;
    @Mock private SearchAliasRepository searchAliasRepository;

    private SearchAliasValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SearchAliasValidator(searchIndexRepository, searchAliasRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        when(searchAliasRepository.findByAliasNameAndDeletedFalse(SrhTestFixtures.ALIAS_NAME))
                .thenReturn(Optional.empty());

        assertThatCode(() -> validator.validateCreate(SrhTestFixtures.aliasCreateRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDuplicateAliasName() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        SearchAlias existing = SrhTestFixtures.searchAlias(UUID.randomUUID(),
                SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID));
        when(searchAliasRepository.findByAliasNameAndDeletedFalse(SrhTestFixtures.ALIAS_NAME))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> validator.validateCreate(SrhTestFixtures.aliasCreateRequest()))
                .isInstanceOf(SearchAliasException.class);
    }

    @Test
    void shouldRejectActiveAliasUniqueness() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        when(searchAliasRepository.findByAliasNameAndDeletedFalse("new-alias"))
                .thenReturn(Optional.empty());
        SearchAlias activeAlias = SrhTestFixtures.searchAlias(UUID.randomUUID(),
                SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID));
        activeAlias.setActiveAlias(true);
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(activeAlias));
        SearchAliasCreateRequest request = new SearchAliasCreateRequest(
                null, SrhTestFixtures.INDEX_ID, "new-alias", "cmp_complaint_v2", true, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchAliasException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        assertThatCode(() -> validator.validateUpdate(SrhTestFixtures.aliasUpdateRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUpdateWhenPhysicalIndexNameMissing() {
        SearchAliasUpdateRequest request = new SearchAliasUpdateRequest(
                null, SrhTestFixtures.ALIAS_NAME, " ", false, true, 0L);

        assertThatThrownBy(() -> validator.validateUpdate(request))
                .isInstanceOf(SearchAliasException.class);
    }

    @Test
    void shouldRejectMissingSearchIndex() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(SrhTestFixtures.aliasCreateRequest()))
                .isInstanceOf(SearchIndexNotFoundException.class);
    }
}
