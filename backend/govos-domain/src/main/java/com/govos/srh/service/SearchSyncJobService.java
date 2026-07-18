package com.govos.srh.service;

import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.dto.SearchSyncJobUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface SearchSyncJobService {

    SearchSyncJobDto create(SearchSyncJobCreateRequest request);

    SearchSyncJobDto update(UUID id, SearchSyncJobUpdateRequest request);

    SearchSyncJobDto getById(UUID id);

    List<SearchSyncJobDto> listByIndex(UUID searchIndexId);

    SearchSyncJobDto start(UUID id);

    SearchSyncJobDto complete(UUID id);

    SearchSyncJobDto fail(UUID id);

    SearchSyncJobDto cancel(UUID id);
}
