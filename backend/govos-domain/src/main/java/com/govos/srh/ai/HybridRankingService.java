package com.govos.srh.ai;

import com.govos.srh.query.SearchResult;

import java.util.List;

public interface HybridRankingService {

    List<SearchResult> rank(
            List<SearchResult> keywordResults,
            List<VectorSearchHit> vectorHits,
            double keywordWeight,
            double semanticWeight);
}
