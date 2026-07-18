package com.govos.srh.service.impl;

import com.govos.srh.dto.IndexSearchDocumentRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.dto.SearchIndexUpdateRequest;
import com.govos.srh.engine.BulkOperationResult;
import com.govos.srh.engine.EngineDocumentRequest;
import com.govos.srh.engine.SearchEngineHealthStatus;
import com.govos.srh.engine.SearchEngineProvider;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.entity.SearchDocument;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.exception.SearchAliasException;
import com.govos.srh.exception.SearchEngineException;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.mapper.SearchIndexMapper;
import com.govos.srh.repository.SearchAliasRepository;
import com.govos.srh.repository.SearchDocumentRepository;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.service.SearchIndexService;
import com.govos.srh.validator.SearchIndexValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchIndexServiceImpl implements SearchIndexService {

    private final SearchIndexRepository searchIndexRepository;
    private final SearchIndexMapper searchIndexMapper;
    private final SearchIndexValidator searchIndexValidator;
    private final SearchEngineProvider searchEngineProvider;
    private final SearchAliasRepository searchAliasRepository;
    private final SearchDocumentRepository searchDocumentRepository;

    public SearchIndexServiceImpl(
            SearchIndexRepository searchIndexRepository,
            SearchIndexMapper searchIndexMapper,
            SearchIndexValidator searchIndexValidator,
            SearchEngineProvider searchEngineProvider,
            SearchAliasRepository searchAliasRepository,
            SearchDocumentRepository searchDocumentRepository) {
        this.searchIndexRepository = searchIndexRepository;
        this.searchIndexMapper = searchIndexMapper;
        this.searchIndexValidator = searchIndexValidator;
        this.searchEngineProvider = searchEngineProvider;
        this.searchAliasRepository = searchAliasRepository;
        this.searchDocumentRepository = searchDocumentRepository;
    }

    @Override
    @Transactional
    public SearchIndexDto create(SearchIndexCreateRequest request) {
        searchIndexValidator.validateCreate(request);

        SearchIndex entity = searchIndexMapper.toEntity(request);
        entity.setCode(request.code());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return searchIndexMapper.toDto(searchIndexRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchIndexDto update(UUID id, SearchIndexUpdateRequest request) {
        SearchIndex entity = findActiveById(id);
        assertVersion(entity, request.version());

        searchIndexValidator.validateUpdate(request);
        searchIndexValidator.validateCodeUniqueness(request.code(), id);
        searchIndexValidator.validateNameUniqueness(request.name(), id);

        searchIndexMapper.updateEntity(request, entity);
        entity.setCode(request.code());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return searchIndexMapper.toDto(searchIndexRepository.save(entity));
    }

    @Override
    public SearchIndexDto getById(UUID id) {
        return searchIndexMapper.toDto(findActiveById(id));
    }

    @Override
    public SearchIndexDto getByCode(String code) {
        return searchIndexMapper.toDto(findActiveByCode(code));
    }

    @Override
    public Page<SearchIndexDto> list(Pageable pageable) {
        List<SearchIndex> indexes = findAllActiveIndexes();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), indexes.size());
        List<SearchIndexDto> content = start >= indexes.size()
                ? List.of()
                : indexes.subList(start, end).stream()
                        .map(searchIndexMapper::toDto)
                        .toList();
        return new PageImpl<>(content, pageable, indexes.size());
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        SearchIndex entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        entity.setStatus(SearchIndexStatus.DELETED);
        searchIndexRepository.save(entity);
    }

    @Override
    @Transactional
    public SearchIndexDto restore(UUID id) {
        SearchIndex entity = searchIndexRepository.findById(id)
                .filter(index -> Boolean.TRUE.equals(index.getDeleted()))
                .orElseThrow(() -> new SearchIndexNotFoundException("Search index not found with id: " + id));
        entity.setDeleted(false);
        entity.setActive(true);
        if (entity.getStatus() == SearchIndexStatus.DELETED) {
            entity.setStatus(SearchIndexStatus.ACTIVE);
        }
        return searchIndexMapper.toDto(searchIndexRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchIndexDto activate(UUID id) {
        SearchIndex entity = findActiveById(id);
        entity.setActive(true);
        if (entity.getStatus() == SearchIndexStatus.ARCHIVED) {
            entity.setStatus(SearchIndexStatus.ACTIVE);
        }
        return searchIndexMapper.toDto(searchIndexRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchIndexDto archive(UUID id) {
        SearchIndex entity = findActiveById(id);
        entity.setActive(false);
        entity.setStatus(SearchIndexStatus.ARCHIVED);
        return searchIndexMapper.toDto(searchIndexRepository.save(entity));
    }

    @Override
    @Transactional
    public SearchIndexDto createPhysicalIndex(UUID id) {
        SearchIndex entity = findActiveById(id);
        String physicalIndexName = resolvePhysicalIndexName(entity);
        entity.setPhysicalIndexName(physicalIndexName);
        searchIndexRepository.save(entity);

        if (!searchEngineProvider.indexExists(physicalIndexName)) {
            searchEngineProvider.createIndex(physicalIndexName);
        }

        return searchIndexMapper.toDto(entity);
    }

    @Override
    public void deletePhysicalIndex(UUID id) {
        SearchIndex entity = findActiveById(id);
        String physicalIndexName = entity.getPhysicalIndexName();
        if (physicalIndexName != null && !physicalIndexName.isBlank()) {
            searchEngineProvider.deleteIndex(physicalIndexName);
        }
    }

    @Override
    public void indexDocument(IndexSearchDocumentRequest request) {
        SearchIndex index = findActiveByCode(request.indexCode());
        String targetIndex = resolveWriteTarget(index);
        searchEngineProvider.indexDocument(
                targetIndex,
                request.documentId().toString(),
                request.documentJson());
    }

    @Override
    public void updateDocument(IndexSearchDocumentRequest request) {
        SearchIndex index = findActiveByCode(request.indexCode());
        String targetIndex = resolveWriteTarget(index);
        searchEngineProvider.updateDocument(
                targetIndex,
                request.documentId().toString(),
                request.documentJson());
    }

    @Override
    public void removeDocument(String indexCode, UUID documentId) {
        SearchIndex index = findActiveByCode(indexCode);
        String targetIndex = resolveWriteTarget(index);
        searchEngineProvider.deleteDocument(targetIndex, documentId.toString());
    }

    @Override
    public BulkOperationResult bulkIndex(UUID searchIndexId, List<SearchDocumentDto> documents) {
        SearchIndex index = findActiveById(searchIndexId);
        String targetIndex = resolveWriteTarget(index);
        List<EngineDocumentRequest> engineDocuments = documents.stream()
                .map(this::toEngineDocument)
                .toList();
        return searchEngineProvider.bulkIndex(targetIndex, engineDocuments);
    }

    @Override
    public BulkOperationResult bulkDelete(UUID searchIndexId, List<UUID> documentIds) {
        SearchIndex index = findActiveById(searchIndexId);
        String targetIndex = resolveWriteTarget(index);
        List<String> ids = documentIds.stream().map(UUID::toString).toList();
        return searchEngineProvider.bulkDelete(targetIndex, ids);
    }

    @Override
    @Transactional
    public SearchIndexDto switchAlias(UUID searchIndexId, UUID aliasId) {
        SearchIndex index = findActiveById(searchIndexId);
        SearchAlias alias = searchAliasRepository.findById(aliasId)
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .filter(item -> item.getSearchIndex().getId().equals(searchIndexId))
                .orElseThrow(() -> new SearchAliasException("Search alias not found with id: " + aliasId));

        String aliasName = alias.getAliasName();
        String oldPhysicalIndexName = alias.getPhysicalIndexName();
        String newPhysicalIndexName = buildNextPhysicalIndexName(index);

        searchEngineProvider.createIndex(newPhysicalIndexName);

        List<SearchDocument> persistedDocuments =
                searchDocumentRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId);
        if (!persistedDocuments.isEmpty()) {
            List<EngineDocumentRequest> engineDocuments = persistedDocuments.stream()
                    .map(this::toEngineDocument)
                    .toList();
            BulkOperationResult bulkResult = searchEngineProvider.bulkIndex(newPhysicalIndexName, engineDocuments);
            if (bulkResult.failureCount() > 0) {
                throw new SearchEngineException(
                        "Bulk copy failed during alias switch for index: " + searchIndexId);
            }
        }

        searchEngineProvider.switchAlias(aliasName, newPhysicalIndexName, oldPhysicalIndexName);

        searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(searchIndexId).stream()
                .filter(item -> Boolean.TRUE.equals(item.getActiveAlias()))
                .filter(item -> !item.getId().equals(aliasId))
                .forEach(item -> {
                    item.setActiveAlias(false);
                    searchAliasRepository.save(item);
                });

        alias.setPhysicalIndexName(newPhysicalIndexName);
        alias.setActiveAlias(true);
        alias.setSwitchedAt(Instant.now());
        searchAliasRepository.save(alias);

        index.setPhysicalIndexName(newPhysicalIndexName);
        index.setMappingVersion(index.getMappingVersion() + 1);
        index.setLastReindexedAt(Instant.now());
        if (oldPhysicalIndexName != null
                && !oldPhysicalIndexName.isBlank()
                && searchEngineProvider.indexExists(oldPhysicalIndexName)) {
            index.setStatus(SearchIndexStatus.ARCHIVED);
        }
        searchIndexRepository.save(index);

        return searchIndexMapper.toDto(index);
    }

    @Override
    public SearchEngineHealthStatus health() {
        return searchEngineProvider.health();
    }

    private SearchIndex findActiveById(UUID id) {
        return searchIndexValidator.requireExists(id);
    }

    private SearchIndex findActiveByCode(String code) {
        return searchIndexRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new SearchIndexNotFoundException("Search index not found with code: " + code));
    }

    private List<SearchIndex> findAllActiveIndexes() {
        Map<UUID, SearchIndex> indexes = new LinkedHashMap<>();
        for (SearchIndexStatus status : SearchIndexStatus.values()) {
            for (SearchIndex index : searchIndexRepository.findAllByStatusAndDeletedFalse(status)) {
                indexes.putIfAbsent(index.getId(), index);
            }
        }
        return new ArrayList<>(indexes.values()).stream()
                .sorted(Comparator.comparing(SearchIndex::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void assertVersion(SearchIndex entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "SearchIndex version mismatch for id: " + entity.getId());
        }
    }

    private String resolveWriteTarget(SearchIndex index) {
        return searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(index.getId()).stream()
                .filter(alias -> Boolean.TRUE.equals(alias.getActiveAlias()))
                .map(SearchAlias::getAliasName)
                .findFirst()
                .orElseGet(() -> {
                    if (index.getPhysicalIndexName() != null && !index.getPhysicalIndexName().isBlank()) {
                        return index.getPhysicalIndexName();
                    }
                    return resolvePhysicalIndexName(index);
                });
    }

    private String resolvePhysicalIndexName(SearchIndex index) {
        if (index.getPhysicalIndexName() != null && !index.getPhysicalIndexName().isBlank()) {
            return index.getPhysicalIndexName();
        }
        return buildPhysicalIndexName(index);
    }

    private String buildPhysicalIndexName(SearchIndex index) {
        String code = index.getCode() != null ? index.getCode().toLowerCase() : index.getId().toString();
        int version = index.getMappingVersion() != null ? index.getMappingVersion() : 1;
        return code + "-v" + version;
    }

    private String buildNextPhysicalIndexName(SearchIndex index) {
        int nextVersion = (index.getMappingVersion() != null ? index.getMappingVersion() : 1) + 1;
        String code = index.getCode() != null ? index.getCode().toLowerCase() : index.getId().toString();
        return code + "-v" + nextVersion;
    }

    private EngineDocumentRequest toEngineDocument(SearchDocumentDto document) {
        String documentId = document.searchDocumentId() != null
                ? document.searchDocumentId().toString()
                : document.id().toString();
        String documentJson = document.documentJson() != null ? document.documentJson() : "{}";
        return new EngineDocumentRequest(documentId, documentJson);
    }

    private EngineDocumentRequest toEngineDocument(SearchDocument document) {
        String documentId = document.getSearchDocumentId() != null
                ? document.getSearchDocumentId().toString()
                : document.getId().toString();
        String documentJson = document.getDocumentJson() != null ? document.getDocumentJson() : "{}";
        return new EngineDocumentRequest(documentId, documentJson);
    }
}
