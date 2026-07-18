package com.govos.srh.ai;

import com.govos.srh.query.SearchResponse;

public interface SemanticSearchService {

    SearchResponse semanticSearch(SemanticSearchRequest request);

    SearchResponse hybridSearch(HybridSearchRequest request);

    SemanticSearchInfo getSemanticInfo();
}
