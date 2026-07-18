package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.entity.SearchDocument;
import com.govos.srh.exception.SearchDocumentException;
import com.govos.srh.mapper.SearchDocumentMapper;
import com.govos.srh.repository.SearchDocumentRepository;
import com.govos.srh.service.SearchDocumentService;
import com.govos.srh.validator.SearchDocumentValidator;
import com.govos.srh.validator.SearchIndexValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchDocumentServiceImpl implements SearchDocumentService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchDocumentMapper searchDocumentMapper;
    private final SearchDocumentValidator searchDocumentValidator;
    private final SearchIndexValidator searchIndexValidator;

    public SearchDocumentServiceImpl(
            SearchDocumentRepository searchDocumentRepository,
            SearchDocumentMapper searchDocumentMapper,
            SearchDocumentValidator searchDocumentValidator,
            SearchIndexValidator searchIndexValidator) {
        this.searchDocumentRepository = searchDocumentRepository;
        this.searchDocumentMapper = searchDocumentMapper;
        this.searchDocumentValidator = searchDocumentValidator;
        this.searchIndexValidator = searchIndexValidator;
    }

    @Override
    @Transactional
    public SearchDocumentDto create(SearchDocumentCreateRequest request) {
        searchDocumentValidator.validateCreate(request);

        SearchDocument entity = searchDocumentMapper.toEntity(request);
        entity.setCode(request.code());
        entity.setSearchIndex(searchIndexValidator.requireExists(request.searchIndexId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return searchDocumentMapper.toDto(searchDocumentRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchDocumentDto update(UUID id, SearchDocumentUpdateRequest request) {
        SearchDocument entity = findActiveById(id);
        assertVersion(entity, request.version());

        searchDocumentValidator.validateUpdate(request);
        searchDocumentMapper.updateEntity(request, entity);
        entity.setCode(request.code());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return searchDocumentMapper.toDto(searchDocumentRepository.save(entity));
    }

    @Override
    public SearchDocumentDto getById(UUID id) {
        return searchDocumentMapper.toDto(findActiveById(id));
    }

    @Override
    public List<SearchDocumentDto> listByIndex(UUID searchIndexId) {
        searchIndexValidator.requireExists(searchIndexId);
        return searchDocumentRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId).stream()
                .map(searchDocumentMapper::toDto)
                .toList();
    }

    @Override
    public List<SearchDocumentDto> listByOrganization(UUID organizationId) {
        return searchDocumentRepository.findAllByOrganizationIdAndDeletedFalse(organizationId).stream()
                .map(searchDocumentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        SearchDocument entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        searchDocumentRepository.save(entity);
    }

    @Override
    @Transactional
    public SearchDocumentDto restore(UUID id) {
        SearchDocument entity = searchDocumentRepository.findById(id)
                .filter(document -> Boolean.TRUE.equals(document.getDeleted()))
                .orElseThrow(() -> new SearchDocumentException("Search document not found with id: " + id));
        entity.setDeleted(false);
        entity.setActive(true);
        return searchDocumentMapper.toDto(searchDocumentRepository.save(entity));
    }

    private SearchDocument findActiveById(UUID id) {
        return searchDocumentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new SearchDocumentException("Search document not found with id: " + id));
    }

    private void assertVersion(SearchDocument entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "SearchDocument version mismatch for id: " + entity.getId());
        }
    }
}
