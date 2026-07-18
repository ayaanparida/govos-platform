package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.dto.SearchSyncJobUpdateRequest;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.entity.SearchSyncJob;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.exception.SearchSyncJobException;
import com.govos.srh.mapper.SearchSyncJobMapper;
import com.govos.srh.repository.SearchSyncJobRepository;
import com.govos.srh.support.SrhTestFixtures;
import com.govos.srh.validator.SearchIndexValidator;
import com.govos.srh.validator.SearchSyncJobValidator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchSyncJobServiceImplTest {

    @Mock private SearchSyncJobRepository searchSyncJobRepository;
    @Mock private SearchSyncJobMapper searchSyncJobMapper;
    @Mock private SearchSyncJobValidator searchSyncJobValidator;
    @Mock private SearchIndexValidator searchIndexValidator;

    @InjectMocks
    private SearchSyncJobServiceImpl service;

    private SearchIndex index;
    private SearchSyncJob job;
    private SearchSyncJobDto dto;

    @BeforeEach
    void setUp() {
        index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        job = SrhTestFixtures.searchSyncJob(SrhTestFixtures.JOB_ID, index);
        dto = minimalDto(job);
    }

    @Test
    void shouldCreateSearchSyncJob() {
        SearchSyncJobCreateRequest request = SrhTestFixtures.syncJobCreateRequest();
        when(searchSyncJobMapper.toEntity(request)).thenReturn(job);
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchSyncJobRepository.save(job)).thenReturn(job);
        when(searchSyncJobMapper.toDto(job)).thenReturn(dto);

        assertThat(service.create(request)).isEqualTo(dto);
        verify(searchSyncJobValidator).validateCreate(request);
        assertThat(job.getDeleted()).isFalse();
        assertThat(job.getSearchIndex()).isEqualTo(index);
    }

    @Test
    void shouldUpdateSearchSyncJob() {
        SearchSyncJobUpdateRequest request = SrhTestFixtures.syncJobUpdateRequest();
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));
        when(searchSyncJobRepository.save(job)).thenReturn(job);
        when(searchSyncJobMapper.toDto(job)).thenReturn(dto);

        assertThat(service.update(SrhTestFixtures.JOB_ID, request)).isEqualTo(dto);
        verify(searchSyncJobValidator).validateUpdate(request);
        verify(searchSyncJobMapper).updateEntity(request, job);
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        job.setVersion(1L);
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.update(SrhTestFixtures.JOB_ID, SrhTestFixtures.syncJobUpdateRequest()))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldGetById() {
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));
        when(searchSyncJobMapper.toDto(job)).thenReturn(dto);

        assertThat(service.getById(SrhTestFixtures.JOB_ID)).isEqualTo(dto);
    }

    @Test
    void shouldListByIndex() {
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchSyncJobRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(job));
        when(searchSyncJobMapper.toDto(job)).thenReturn(dto);

        assertThat(service.listByIndex(SrhTestFixtures.INDEX_ID)).containsExactly(dto);
    }

    @Test
    void shouldStartJob() {
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));
        when(searchSyncJobRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(job));
        when(searchSyncJobRepository.save(job)).thenReturn(job);
        when(searchSyncJobMapper.toDto(job)).thenReturn(dto);

        assertThat(service.start(SrhTestFixtures.JOB_ID)).isEqualTo(dto);
        assertThat(job.getStatus()).isEqualTo(SearchJobStatus.RUNNING);
        assertThat(job.getStartedAt()).isNotNull();
    }

    @Test
    void shouldCompleteJob() {
        job.setStatus(SearchJobStatus.RUNNING);
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));
        when(searchSyncJobRepository.save(job)).thenReturn(job);
        when(searchSyncJobMapper.toDto(job)).thenReturn(dto);

        assertThat(service.complete(SrhTestFixtures.JOB_ID)).isEqualTo(dto);
        assertThat(job.getStatus()).isEqualTo(SearchJobStatus.COMPLETED);
        assertThat(job.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldFailJob() {
        job.setStatus(SearchJobStatus.RUNNING);
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));
        when(searchSyncJobRepository.save(job)).thenReturn(job);
        when(searchSyncJobMapper.toDto(job)).thenReturn(dto);

        assertThat(service.fail(SrhTestFixtures.JOB_ID)).isEqualTo(dto);
        assertThat(job.getStatus()).isEqualTo(SearchJobStatus.FAILED);
    }

    @Test
    void shouldCancelJob() {
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));
        when(searchSyncJobRepository.save(job)).thenReturn(job);
        when(searchSyncJobMapper.toDto(job)).thenReturn(dto);

        assertThat(service.cancel(SrhTestFixtures.JOB_ID)).isEqualTo(dto);
        assertThat(job.getStatus()).isEqualTo(SearchJobStatus.CANCELLED);
    }

    @Test
    void shouldRejectStartWhenNotPending() {
        job.setStatus(SearchJobStatus.RUNNING);
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.start(SrhTestFixtures.JOB_ID))
                .isInstanceOf(SearchSyncJobException.class);
    }

    @Test
    void shouldRejectCompleteWhenNotRunning() {
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.complete(SrhTestFixtures.JOB_ID))
                .isInstanceOf(SearchSyncJobException.class);
    }

    @Test
    void shouldRejectStartWhenAnotherRunningJobExists() {
        SearchSyncJob other = SrhTestFixtures.searchSyncJob(UUID.randomUUID(), index);
        other.setStatus(SearchJobStatus.RUNNING);
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.of(job));
        when(searchSyncJobRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(job, other));

        assertThatThrownBy(() -> service.start(SrhTestFixtures.JOB_ID))
                .isInstanceOf(SearchSyncJobException.class);
    }

    @Test
    void shouldThrowWhenJobNotFound() {
        when(searchSyncJobRepository.findByIdAndDeletedFalse(SrhTestFixtures.JOB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(SrhTestFixtures.JOB_ID))
                .isInstanceOf(SearchSyncJobException.class);
    }

    private SearchSyncJobDto minimalDto(SearchSyncJob entity) {
        return new SearchSyncJobDto(
                entity.getId(), entity.getCode(), SrhTestFixtures.INDEX_ID, entity.getJobName(),
                entity.getJobType(), entity.getStatus(), entity.getStartedAt(), entity.getCompletedAt(),
                entity.getProcessedCount(), entity.getSuccessCount(), entity.getFailureCount(),
                entity.getErrorMessage(), entity.getActive(), entity.getVersion(),
                null, null, null, null);
    }
}
