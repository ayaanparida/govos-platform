package com.govos.srh.validator;

import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexUpdateRequest;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.exception.SearchIndexAlreadyExistsException;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.exception.SearchIndexValidationException;
import com.govos.srh.repository.SearchIndexRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class SearchIndexValidator {

    private final SearchIndexRepository searchIndexRepository;

    public SearchIndexValidator(SearchIndexRepository searchIndexRepository) {
        this.searchIndexRepository = searchIndexRepository;
    }

    public SearchIndex requireExists(UUID id) {
        return searchIndexRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new SearchIndexNotFoundException("Search index not found with id: " + id));
    }

    public void validateCreate(SearchIndexCreateRequest request) {
        validateNameRequired(request.name());
        validateEngineTypeRequired(request.engineType());
        validateMappingVersionPositive(request.mappingVersion());
        validateCodeUniqueness(request.code());
        validateNameUniqueness(request.name());
    }

    public void validateUpdate(SearchIndexUpdateRequest request) {
        validateNameRequired(request.name());
        validateEngineTypeRequired(request.engineType());
        validateMappingVersionPositive(request.mappingVersion());
    }

    public void validateCodeUniqueness(String code) {
        if (StringUtils.hasText(code) && searchIndexRepository.existsByCodeAndDeletedFalse(code)) {
            throw new SearchIndexAlreadyExistsException("Search index code already exists: " + code);
        }
    }

    public void validateCodeUniqueness(String code, UUID excludeId) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        searchIndexRepository.findByCodeAndDeletedFalse(code)
                .filter(index -> !index.getId().equals(excludeId))
                .ifPresent(index -> {
                    throw new SearchIndexAlreadyExistsException("Search index code already exists: " + code);
                });
    }

    public void validateNameUniqueness(String name) {
        if (StringUtils.hasText(name) && existsByNameAndDeletedFalse(name, null)) {
            throw new SearchIndexAlreadyExistsException("Search index name already exists: " + name);
        }
    }

    public void validateNameUniqueness(String name, UUID excludeId) {
        if (StringUtils.hasText(name) && existsByNameAndDeletedFalse(name, excludeId)) {
            throw new SearchIndexAlreadyExistsException("Search index name already exists: " + name);
        }
    }

    public void validateNotDeleted(SearchIndex searchIndex) {
        if (Boolean.TRUE.equals(searchIndex.getDeleted())) {
            throw new SearchIndexValidationException("Search index is deleted: " + searchIndex.getId());
        }
    }

    private boolean existsByNameAndDeletedFalse(String name, UUID excludeId) {
        for (SearchIndexStatus status : SearchIndexStatus.values()) {
            boolean found = searchIndexRepository.findAllByStatusAndDeletedFalse(status).stream()
                    .filter(index -> excludeId == null || !index.getId().equals(excludeId))
                    .anyMatch(index -> name.equals(index.getName()));
            if (found) {
                return true;
            }
        }
        return false;
    }

    private void validateNameRequired(String name) {
        if (!StringUtils.hasText(name)) {
            throw new SearchIndexValidationException("Search index name is required");
        }
    }

    private void validateEngineTypeRequired(SearchEngineType engineType) {
        if (engineType == null) {
            throw new SearchIndexValidationException("Search index engine type is required");
        }
    }

    private void validateMappingVersionPositive(Integer mappingVersion) {
        if (mappingVersion == null || mappingVersion <= 0) {
            throw new SearchIndexValidationException("Search index mapping version must be greater than zero");
        }
    }
}
