package com.govos.srh.service;

import com.govos.srh.dto.SearchAliasCreateRequest;
import com.govos.srh.dto.SearchAliasDto;
import com.govos.srh.dto.SearchAliasUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface SearchAliasService {

    SearchAliasDto create(SearchAliasCreateRequest request);

    SearchAliasDto update(UUID id, SearchAliasUpdateRequest request);

    SearchAliasDto getByAlias(String aliasName);

    List<SearchAliasDto> listByIndex(UUID searchIndexId);

    SearchAliasDto activateAlias(UUID id);

    void softDelete(UUID id);

    SearchAliasDto restore(UUID id);
}
