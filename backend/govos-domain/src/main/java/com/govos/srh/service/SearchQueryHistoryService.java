package com.govos.srh.service;

import com.govos.srh.dto.SearchQueryHistoryDto;

import java.util.List;
import java.util.UUID;

public interface SearchQueryHistoryService {

    SearchQueryHistoryDto record(SearchQueryHistoryDto request);

    List<SearchQueryHistoryDto> listByOrganization(UUID organizationId);

    List<SearchQueryHistoryDto> listByUser(UUID userId);
}
