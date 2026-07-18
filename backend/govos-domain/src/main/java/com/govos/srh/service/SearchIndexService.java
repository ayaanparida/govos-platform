package com.govos.srh.service;

import com.govos.srh.dto.IndexSearchDocumentRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.dto.SearchIndexUpdateRequest;
import com.govos.srh.engine.BulkOperationResult;
import com.govos.srh.engine.SearchEngineHealthStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SearchIndexService {

    SearchIndexDto create(SearchIndexCreateRequest request);

    SearchIndexDto update(UUID id, SearchIndexUpdateRequest request);

    SearchIndexDto getById(UUID id);

    SearchIndexDto getByCode(String code);

    Page<SearchIndexDto> list(Pageable pageable);

    void softDelete(UUID id);

    SearchIndexDto restore(UUID id);

    SearchIndexDto activate(UUID id);

    SearchIndexDto archive(UUID id);

    SearchIndexDto createPhysicalIndex(UUID id);

    void deletePhysicalIndex(UUID id);

    void indexDocument(IndexSearchDocumentRequest request);

    void updateDocument(IndexSearchDocumentRequest request);

    void removeDocument(String indexCode, UUID documentId);

    BulkOperationResult bulkIndex(UUID searchIndexId, List<SearchDocumentDto> documents);

    BulkOperationResult bulkDelete(UUID searchIndexId, List<UUID> documentIds);

    SearchIndexDto switchAlias(UUID searchIndexId, UUID aliasId);

    SearchEngineHealthStatus health();
}
