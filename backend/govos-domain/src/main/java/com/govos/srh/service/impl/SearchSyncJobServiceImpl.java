package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.dto.SearchSyncJobUpdateRequest;
import com.govos.srh.entity.SearchSyncJob;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.exception.SearchSyncJobException;
import com.govos.srh.mapper.SearchSyncJobMapper;
import com.govos.srh.repository.SearchSyncJobRepository;
import com.govos.srh.service.SearchSyncJobService;
import com.govos.srh.validator.SearchIndexValidator;
import com.govos.srh.validator.SearchSyncJobValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchSyncJobServiceImpl implements SearchSyncJobService {

    private final SearchSyncJobRepository searchSyncJobRepository;
    private final SearchSyncJobMapper searchSyncJobMapper;
    private final SearchSyncJobValidator searchSyncJobValidator;
    private final SearchIndexValidator searchIndexValidator;

    public SearchSyncJobServiceImpl(
            SearchSyncJobRepository searchSyncJobRepository,
            SearchSyncJobMapper searchSyncJobMapper,
            SearchSyncJobValidator searchSyncJobValidator,
            SearchIndexValidator searchIndexValidator) {
        this.searchSyncJobRepository = searchSyncJobRepository;
        this.searchSyncJobMapper = searchSyncJobMapper;
        this.searchSyncJobValidator = searchSyncJobValidator;
        this.searchIndexValidator = searchIndexValidator;
    }

    @Override
    @Transactional
    public SearchSyncJobDto create(SearchSyncJobCreateRequest request) {
        searchSyncJobValidator.validateCreate(request);

        SearchSyncJob entity = searchSyncJobMapper.toEntity(request);
        entity.setCode(request.code());
        entity.setSearchIndex(searchIndexValidator.requireExists(request.searchIndexId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return searchSyncJobMapper.toDto(searchSyncJobRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchSyncJobDto update(UUID id, SearchSyncJobUpdateRequest request) {
        SearchSyncJob entity = findActiveById(id);
        assertVersion(entity, request.version());

        searchSyncJobValidator.validateUpdate(request);
        searchSyncJobMapper.updateEntity(request, entity);
        entity.setCode(request.code());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return searchSyncJobMapper.toDto(searchSyncJobRepository.save(entity));
    }

    @Override
    public SearchSyncJobDto getById(UUID id) {
        return searchSyncJobMapper.toDto(findActiveById(id));
    }

    @Override
    public List<SearchSyncJobDto> listByIndex(UUID searchIndexId) {
        searchIndexValidator.requireExists(searchIndexId);
        return searchSyncJobRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId).stream()
                .map(searchSyncJobMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SearchSyncJobDto start(UUID id) {
        SearchSyncJob entity = findActiveById(id);
        if (entity.getStatus() != SearchJobStatus.PENDING) {
            throw new SearchSyncJobException(
                    "Search sync job can only be started from PENDING status; current="
                            + entity.getStatus() + " for id: " + id);
        }
        validateNoOtherRunningJob(entity.getSearchIndex().getId(), id);

        entity.setStatus(SearchJobStatus.RUNNING);
        entity.setStartedAt(Instant.now());
        return searchSyncJobMapper.toDto(searchSyncJobRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchSyncJobDto complete(UUID id) {
        SearchSyncJob entity = findActiveById(id);
        requireRunningStatus(entity, id);

        entity.setStatus(SearchJobStatus.COMPLETED);
        entity.setCompletedAt(Instant.now());
        return searchSyncJobMapper.toDto(searchSyncJobRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchSyncJobDto fail(UUID id) {
        SearchSyncJob entity = findActiveById(id);
        requireRunningStatus(entity, id);

        entity.setStatus(SearchJobStatus.FAILED);
        entity.setCompletedAt(Instant.now());
        return searchSyncJobMapper.toDto(searchSyncJobRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchSyncJobDto cancel(UUID id) {
        SearchSyncJob entity = findActiveById(id);
        if (entity.getStatus() != SearchJobStatus.PENDING && entity.getStatus() != SearchJobStatus.RUNNING) {
            throw new SearchSyncJobException(
                    "Search sync job can only be cancelled from PENDING or RUNNING status; current="
                            + entity.getStatus() + " for id: " + id);
        }

        entity.setStatus(SearchJobStatus.CANCELLED);
        entity.setCompletedAt(Instant.now());
        return searchSyncJobMapper.toDto(searchSyncJobRepository.save(entity));
    }

    private SearchSyncJob findActiveById(UUID id) {
        return searchSyncJobRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new SearchSyncJobException("Search sync job not found with id: " + id));
    }

    private void requireRunningStatus(SearchSyncJob entity, UUID id) {
        if (entity.getStatus() != SearchJobStatus.RUNNING) {
            throw new SearchSyncJobException(
                    "Search sync job requires RUNNING status; current="
                            + entity.getStatus() + " for id: " + id);
        }
    }

    private void validateNoOtherRunningJob(UUID searchIndexId, UUID excludeId) {
        boolean hasRunningJob = searchSyncJobRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId).stream()
                .filter(job -> !job.getId().equals(excludeId))
                .anyMatch(job -> SearchJobStatus.RUNNING == job.getStatus());
        if (hasRunningJob) {
            throw new SearchSyncJobException(
                    "A running search sync job already exists for search index: " + searchIndexId);
        }
    }

    private void assertVersion(SearchSyncJob entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "SearchSyncJob version mismatch for id: " + entity.getId());
        }
    }
}
