package com.govos.srh.service;

import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchDocumentUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface SearchDocumentService {

    SearchDocumentDto create(SearchDocumentCreateRequest request);

    SearchDocumentDto update(UUID id, SearchDocumentUpdateRequest request);

    SearchDocumentDto getById(UUID id);

    List<SearchDocumentDto> listByIndex(UUID searchIndexId);

    List<SearchDocumentDto> listByOrganization(UUID organizationId);

    void softDelete(UUID id);

    SearchDocumentDto restore(UUID id);
}
