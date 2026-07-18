package com.govos.srh.ai;

import com.govos.srh.query.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HybridRankingServiceTest {

    private HybridRankingService hybridRankingService;

    @BeforeEach
    void setUp() {
        hybridRankingService = new HybridRankingServiceImpl();
    }

    @Test
    void shouldCombineKeywordAndVectorScores() {
        List<SearchResult> keywordResults = List.of(
                new SearchResult("doc-1", 10.0, Map.of("title", "water leak"), null),
                new SearchResult("doc-2", 5.0, Map.of("title", "pothole"), null));

        List<VectorSearchHit> vectorHits = List.of(
                new VectorSearchHit("doc-2", UUID.randomUUID(), UUID.randomUUID(), "COMPLAINT", 0.95,
                        Map.of("entityType", "COMPLAINT")),
                new VectorSearchHit("doc-3", UUID.randomUUID(), UUID.randomUUID(), "COMPLAINT", 0.80,
                        Map.of("entityType", "COMPLAINT")));

        List<SearchResult> ranked = hybridRankingService.rank(keywordResults, vectorHits, 0.70, 0.30);

        assertThat(ranked).hasSize(3);
        assertThat(ranked).extracting(SearchResult::id).contains("doc-1", "doc-2", "doc-3");
        assertThat(ranked.getFirst().score()).isNotNull().isGreaterThan(0D);
    }

    @Test
    void shouldReturnKeywordResultsWhenVectorHitsEmpty() {
        List<SearchResult> keywordResults = List.of(
                new SearchResult("doc-1", 8.0, Map.of("title", "water"), null));

        List<SearchResult> ranked = hybridRankingService.rank(keywordResults, List.of(), 0.70, 0.30);

        assertThat(ranked).hasSize(1);
        assertThat(ranked.getFirst().id()).isEqualTo("doc-1");
    }

    @Test
    void shouldReturnVectorResultsWhenKeywordResultsEmpty() {
        List<VectorSearchHit> vectorHits = List.of(
                new VectorSearchHit("doc-9", UUID.randomUUID(), UUID.randomUUID(), "COMPLAINT", 0.75,
                        Map.of("entityType", "COMPLAINT")));

        List<SearchResult> ranked = hybridRankingService.rank(List.of(), vectorHits, 0.70, 0.30);

        assertThat(ranked).hasSize(1);
        assertThat(ranked.getFirst().id()).isEqualTo("doc-9");
    }
}
