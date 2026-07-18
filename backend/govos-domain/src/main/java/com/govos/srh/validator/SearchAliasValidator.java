package com.govos.srh.validator;

import com.govos.srh.dto.SearchAliasCreateRequest;
import com.govos.srh.dto.SearchAliasUpdateRequest;
import com.govos.srh.exception.SearchAliasException;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.repository.SearchAliasRepository;
import com.govos.srh.repository.SearchIndexRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class SearchAliasValidator {

    private final SearchIndexRepository searchIndexRepository;
    private final SearchAliasRepository searchAliasRepository;

    public SearchAliasValidator(
            SearchIndexRepository searchIndexRepository,
            SearchAliasRepository searchAliasRepository) {
        this.searchIndexRepository = searchIndexRepository;
        this.searchAliasRepository = searchAliasRepository;
    }

    public void validateCreate(SearchAliasCreateRequest request) {
        requireSearchIndex(request.searchIndexId());
        validateAliasNameRequired(request.aliasName());
        validateAliasNameUniqueness(request.aliasName(), null);
        validateActiveAliasUniqueness(request.searchIndexId(), request.activeAlias(), null);
    }

    public void validateUpdate(SearchAliasUpdateRequest request) {
        validateAliasNameRequired(request.aliasName());
        validatePhysicalIndexNameRequired(request.physicalIndexName());
    }

    private void requireSearchIndex(UUID searchIndexId) {
        if (searchIndexId == null) {
            throw new SearchAliasException("Search index is required for search alias");
        }
        searchIndexRepository.findByIdAndDeletedFalse(searchIndexId)
                .orElseThrow(() -> new SearchIndexNotFoundException(
                        "Search index not found with id: " + searchIndexId));
    }

    private void validateAliasNameRequired(String aliasName) {
        if (!StringUtils.hasText(aliasName)) {
            throw new SearchAliasException("Search alias name is required");
        }
    }

    private void validatePhysicalIndexNameRequired(String physicalIndexName) {
        if (!StringUtils.hasText(physicalIndexName)) {
            throw new SearchAliasException("Search alias physical index name is required");
        }
    }

    private void validateAliasNameUniqueness(String aliasName, UUID excludeId) {
        if (!StringUtils.hasText(aliasName)) {
            return;
        }
        searchAliasRepository.findByAliasNameAndDeletedFalse(aliasName)
                .filter(alias -> excludeId == null || !alias.getId().equals(excludeId))
                .ifPresent(alias -> {
                    throw new SearchAliasException("Search alias name already exists: " + aliasName);
                });
    }

    private void validateActiveAliasUniqueness(UUID searchIndexId, Boolean activeAlias, UUID excludeId) {
        if (!Boolean.TRUE.equals(activeAlias)) {
            return;
        }
        searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId).stream()
                .filter(alias -> Boolean.TRUE.equals(alias.getActiveAlias()))
                .filter(alias -> excludeId == null || !alias.getId().equals(excludeId))
                .findFirst()
                .ifPresent(alias -> {
                    throw new SearchAliasException(
                            "Active search alias already exists for search index: " + searchIndexId);
                });
    }
}
