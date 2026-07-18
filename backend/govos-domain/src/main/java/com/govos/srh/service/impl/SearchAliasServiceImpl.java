package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchAliasCreateRequest;
import com.govos.srh.dto.SearchAliasDto;
import com.govos.srh.dto.SearchAliasUpdateRequest;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.exception.SearchAliasException;
import com.govos.srh.mapper.SearchAliasMapper;
import com.govos.srh.repository.SearchAliasRepository;
import com.govos.srh.service.SearchAliasService;
import com.govos.srh.validator.SearchAliasValidator;
import com.govos.srh.validator.SearchIndexValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchAliasServiceImpl implements SearchAliasService {

    private final SearchAliasRepository searchAliasRepository;
    private final SearchAliasMapper searchAliasMapper;
    private final SearchAliasValidator searchAliasValidator;
    private final SearchIndexValidator searchIndexValidator;

    public SearchAliasServiceImpl(
            SearchAliasRepository searchAliasRepository,
            SearchAliasMapper searchAliasMapper,
            SearchAliasValidator searchAliasValidator,
            SearchIndexValidator searchIndexValidator) {
        this.searchAliasRepository = searchAliasRepository;
        this.searchAliasMapper = searchAliasMapper;
        this.searchAliasValidator = searchAliasValidator;
        this.searchIndexValidator = searchIndexValidator;
    }

    @Override
    @Transactional
    public SearchAliasDto create(SearchAliasCreateRequest request) {
        searchAliasValidator.validateCreate(request);

        SearchAlias entity = searchAliasMapper.toEntity(request);
        entity.setCode(request.code());
        entity.setSearchIndex(searchIndexValidator.requireExists(request.searchIndexId()));
        entity.setActiveAlias(request.activeAlias() != null ? request.activeAlias() : false);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return searchAliasMapper.toDto(searchAliasRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchAliasDto update(UUID id, SearchAliasUpdateRequest request) {
        SearchAlias entity = findActiveById(id);
        assertVersion(entity, request.version());

        searchAliasValidator.validateUpdate(request);
        searchAliasMapper.updateEntity(request, entity);
        entity.setCode(request.code());
        if (request.activeAlias() != null) {
            entity.setActiveAlias(request.activeAlias());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return searchAliasMapper.toDto(searchAliasRepository.save(entity));
    }

    @Override
    public SearchAliasDto getByAlias(String aliasName) {
        return searchAliasMapper.toDto(findActiveByAliasName(aliasName));
    }

    @Override
    public List<SearchAliasDto> listByIndex(UUID searchIndexId) {
        searchIndexValidator.requireExists(searchIndexId);
        return searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId).stream()
                .map(searchAliasMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SearchAliasDto activateAlias(UUID id) {
        SearchAlias entity = findActiveById(id);
        UUID searchIndexId = entity.getSearchIndex().getId();

        searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId).stream()
                .filter(alias -> Boolean.TRUE.equals(alias.getActiveAlias()))
                .filter(alias -> !alias.getId().equals(id))
                .forEach(alias -> {
                    alias.setActiveAlias(false);
                    searchAliasRepository.save(alias);
                });

        entity.setActiveAlias(true);
        entity.setSwitchedAt(Instant.now());
        return searchAliasMapper.toDto(searchAliasRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        SearchAlias entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        entity.setActiveAlias(false);
        searchAliasRepository.save(entity);
    }

    @Override
    @Transactional
    public SearchAliasDto restore(UUID id) {
        SearchAlias entity = searchAliasRepository.findById(id)
                .filter(alias -> Boolean.TRUE.equals(alias.getDeleted()))
                .orElseThrow(() -> new SearchAliasException("Search alias not found with id: " + id));
        entity.setDeleted(false);
        entity.setActive(true);
        return searchAliasMapper.toDto(searchAliasRepository.save(entity));
    }

    private SearchAlias findActiveById(UUID id) {
        return searchAliasRepository.findById(id)
                .filter(alias -> !Boolean.TRUE.equals(alias.getDeleted()))
                .orElseThrow(() -> new SearchAliasException("Search alias not found with id: " + id));
    }

    private SearchAlias findActiveByAliasName(String aliasName) {
        return searchAliasRepository.findByAliasNameAndDeletedFalse(aliasName)
                .orElseThrow(() -> new SearchAliasException("Search alias not found with name: " + aliasName));
    }

    private void assertVersion(SearchAlias entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "SearchAlias version mismatch for id: " + entity.getId());
        }
    }
}
