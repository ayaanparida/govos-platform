package com.govos.srh.engine;

import com.govos.srh.query.SearchFilters;
import com.govos.srh.query.SearchQueryMode;
import com.govos.srh.query.SearchSort;

import java.util.List;
import java.util.UUID;

public record EngineAdvancedSearchRequest(
        String indexName,
        UUID organizationId,
        String queryText,
        SearchQueryMode queryMode,
        SearchFilters filters,
        int from,
        int size,
        List<SearchSort> sort,
        boolean highlight,
        List<String> facetFields,
        Double latitude,
        Double longitude,
        Double radiusKm,
        Double topLeftLatitude,
        Double topLeftLongitude,
        Double bottomRightLatitude,
        Double bottomRightLongitude,
        boolean sortByDistance,
        long timeoutMs
) {
}
