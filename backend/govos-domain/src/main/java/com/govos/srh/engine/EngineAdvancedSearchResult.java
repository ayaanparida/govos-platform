package com.govos.srh.engine;

import java.util.List;

public record EngineAdvancedSearchResult(
        long totalHits,
        List<EngineSearchHit> hits,
        List<EngineFacetResult> facets
) {
}
