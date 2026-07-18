package com.govos.srh.validator;

import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobUpdateRequest;
import com.govos.srh.enums.SearchJobType;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.exception.SearchSyncJobException;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.repository.SearchSyncJobRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class SearchSyncJobValidator {

    private final SearchIndexRepository searchIndexRepository;
    private final SearchSyncJobRepository searchSyncJobRepository;

    public SearchSyncJobValidator(
            SearchIndexRepository searchIndexRepository,
            SearchSyncJobRepository searchSyncJobRepository) {
        this.searchIndexRepository = searchIndexRepository;
        this.searchSyncJobRepository = searchSyncJobRepository;
    }

    public void validateCreate(SearchSyncJobCreateRequest request) {
        requireSearchIndex(request.searchIndexId());
        validateJobTypeRequired(request.jobType());
        validateJobNameRequired(request.jobName());
        validateNoActiveRunningJob(request.searchIndexId());
    }

    public void validateUpdate(SearchSyncJobUpdateRequest request) {
        validateJobTypeRequired(request.jobType());
        validateJobNameRequired(request.jobName());
    }

    private void requireSearchIndex(UUID searchIndexId) {
        if (searchIndexId == null) {
            throw new SearchSyncJobException("Search index is required for search sync job");
        }
        searchIndexRepository.findByIdAndDeletedFalse(searchIndexId)
                .orElseThrow(() -> new SearchIndexNotFoundException(
                        "Search index not found with id: " + searchIndexId));
    }

    private void validateJobTypeRequired(SearchJobType jobType) {
        if (jobType == null) {
            throw new SearchSyncJobException("Search sync job type is required");
        }
    }

    private void validateJobNameRequired(String jobName) {
        if (!StringUtils.hasText(jobName)) {
            throw new SearchSyncJobException("Search sync job name is required");
        }
    }

    private void validateNoActiveRunningJob(UUID searchIndexId) {
        boolean hasRunningJob = searchSyncJobRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId).stream()
                .anyMatch(job -> SearchJobStatus.RUNNING == job.getStatus());
        if (hasRunningJob) {
            throw new SearchSyncJobException(
                    "A running search sync job already exists for search index: " + searchIndexId);
        }
    }
}
