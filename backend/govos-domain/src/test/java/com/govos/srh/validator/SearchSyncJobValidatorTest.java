package com.govos.srh.validator;

import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.entity.SearchSyncJob;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.exception.SearchSyncJobException;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.repository.SearchSyncJobRepository;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchSyncJobValidatorTest {

    @Mock private SearchIndexRepository searchIndexRepository;
    @Mock private SearchSyncJobRepository searchSyncJobRepository;

    private SearchSyncJobValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SearchSyncJobValidator(searchIndexRepository, searchSyncJobRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        when(searchSyncJobRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of());

        assertThatCode(() -> validator.validateCreate(SrhTestFixtures.syncJobCreateRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDuplicateRunningJob() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        SearchSyncJob running = SrhTestFixtures.searchSyncJob(SrhTestFixtures.JOB_ID,
                SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID));
        running.setStatus(SearchJobStatus.RUNNING);
        when(searchSyncJobRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(running));

        assertThatThrownBy(() -> validator.validateCreate(SrhTestFixtures.syncJobCreateRequest()))
                .isInstanceOf(SearchSyncJobException.class);
    }

    @Test
    void shouldRejectInvalidJobType() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        SearchSyncJobCreateRequest request = new SearchSyncJobCreateRequest(
                null, SrhTestFixtures.INDEX_ID, "Job", null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchSyncJobException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        assertThatCode(() -> validator.validateUpdate(SrhTestFixtures.syncJobUpdateRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingJobName() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID)));
        SearchSyncJobCreateRequest request = new SearchSyncJobCreateRequest(
                null, SrhTestFixtures.INDEX_ID, "", SearchJobType.FULL_REINDEX, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(SearchSyncJobException.class);
    }

    @Test
    void shouldRejectMissingSearchIndex() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(SrhTestFixtures.syncJobCreateRequest()))
                .isInstanceOf(SearchIndexNotFoundException.class);
    }
}
