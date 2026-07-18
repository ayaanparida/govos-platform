package com.govos.srh.dto;

import com.govos.srh.enums.SearchQueryType;

import java.time.Instant;
import java.util.UUID;

public record SearchQueryHistoryDto(
        UUID id,
        String code,
        UUID organizationId,
        UUID userId,
        String queryText,
        SearchQueryType queryType,
        String filtersJson,
        Long resultCount,
        Long executionTimeMs,
        Instant searchedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
