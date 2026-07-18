package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.enums.SearchQueryType;
import com.govos.srh.mapper.SearchQueryHistoryMapper;
import com.govos.srh.repository.SearchQueryHistoryRepository;
import com.govos.srh.service.SearchQueryHistoryService;
import com.govos.srh.validator.SearchQueryHistoryValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchQueryHistoryServiceImpl implements SearchQueryHistoryService {

    private final SearchQueryHistoryRepository searchQueryHistoryRepository;
    private final SearchQueryHistoryMapper searchQueryHistoryMapper;
    private final SearchQueryHistoryValidator searchQueryHistoryValidator;

    public SearchQueryHistoryServiceImpl(
            SearchQueryHistoryRepository searchQueryHistoryRepository,
            SearchQueryHistoryMapper searchQueryHistoryMapper,
            SearchQueryHistoryValidator searchQueryHistoryValidator) {
        this.searchQueryHistoryRepository = searchQueryHistoryRepository;
        this.searchQueryHistoryMapper = searchQueryHistoryMapper;
        this.searchQueryHistoryValidator = searchQueryHistoryValidator;
    }

    @Override
    @Transactional
    public SearchQueryHistoryDto record(SearchQueryHistoryDto request) {
        SearchQueryHistory entity = new SearchQueryHistory();
        entity.setCode(request.code());
        entity.setOrganizationId(request.organizationId());
        entity.setUserId(request.userId());
        entity.setQueryText(request.queryText());
        entity.setQueryType(request.queryType() != null ? request.queryType() : SearchQueryType.SEARCH);
        entity.setFiltersJson(request.filtersJson());
        entity.setResultCount(request.resultCount() != null ? request.resultCount() : 0L);
        entity.setExecutionTimeMs(request.executionTimeMs() != null ? request.executionTimeMs() : 0L);
        entity.setSearchedAt(request.searchedAt() != null ? request.searchedAt() : Instant.now());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        searchQueryHistoryValidator.validatePersist(entity);

        return searchQueryHistoryMapper.toDto(searchQueryHistoryRepository.save(entity));
    }

    @Override
    public List<SearchQueryHistoryDto> listByOrganization(UUID organizationId) {
        return searchQueryHistoryRepository.findAllByOrganizationIdAndDeletedFalse(organizationId).stream()
                .map(searchQueryHistoryMapper::toDto)
                .toList();
    }

    @Override
    public List<SearchQueryHistoryDto> listByUser(UUID userId) {
        return searchQueryHistoryRepository.findAllByUserIdAndDeletedFalse(userId).stream()
                .map(searchQueryHistoryMapper::toDto)
                .toList();
    }
}
