package com.govos.srh.engine;

import com.govos.srh.query.SearchFilters;
import com.govos.srh.query.SearchQueryMode;

import java.util.UUID;

public record EngineCountRequest(
        String indexName,
        UUID organizationId,
        String queryText,
        SearchQueryMode queryMode,
        SearchFilters filters,
        long timeoutMs
) {
}
