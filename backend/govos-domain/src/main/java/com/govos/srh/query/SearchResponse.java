package com.govos.srh.query;

import java.util.List;

public record SearchResponse(
        long totalHits,
        List<SearchResult> results,
        List<FacetResult> facets,
        SearchPage page,
        long executionTimeMs
) {
}
