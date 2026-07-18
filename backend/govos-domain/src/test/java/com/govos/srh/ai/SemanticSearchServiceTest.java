package com.govos.srh.ai;

import com.govos.srh.ai.provider.EmbeddingCache;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.config.SemanticSearchProperties;
import com.govos.srh.query.SearchPage;
import com.govos.srh.query.SearchQueryService;
import com.govos.srh.query.SearchResponse;
import com.govos.srh.query.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticSearchServiceTest {

    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private SearchQueryService searchQueryService;

    private EmbeddingProvider embeddingProvider;
    private InMemoryVectorIndexService vectorIndexService;
    private HybridRankingService hybridRankingService;
    private SearchProperties searchProperties;
    private SemanticSearchService semanticSearchService;

    @BeforeEach
    void setUp() {
        embeddingProvider = new MockEmbeddingProvider(384);
        vectorIndexService = new InMemoryVectorIndexService();
        hybridRankingService = new HybridRankingServiceImpl();
        searchProperties = new SearchProperties();
        SemanticSearchProperties semantic = new SemanticSearchProperties();
        semantic.setEnabled(true);
        semantic.setKeywordWeight(0.70);
        semantic.setVectorWeight(0.30);
        semantic.setTopK(10);
        searchProperties.setSemantic(semantic);

        semanticSearchService = new SemanticSearchServiceImpl(
                embeddingProvider,
                vectorIndexService,
                hybridRankingService,
                searchQueryService,
                searchProperties,
                new EmbeddingCache(searchProperties));

        seedEmbeddings();
    }

    @Test
    void shouldExecuteSemanticSearch() {
        SemanticSearchRequest request = new SemanticSearchRequest(
                "CMP_COMPLAINT", ORG_ID, null, "water leak", null, SearchPage.defaults(), null);

        SearchResponse response = semanticSearchService.semanticSearch(request);

        assertThat(response.totalHits()).isGreaterThan(0);
        assertThat(response.results()).isNotEmpty();
        assertThat(response.results().getFirst().score()).isGreaterThan(0D);
    }

    @Test
    void shouldRejectSemanticSearchWhenDisabled() {
        searchProperties.getSemantic().setEnabled(false);

        SemanticSearchRequest request = new SemanticSearchRequest(
                "CMP_COMPLAINT", ORG_ID, null, "water leak", null, SearchPage.defaults(), null);

        assertThatThrownBy(() -> semanticSearchService.semanticSearch(request))
                .isInstanceOf(SemanticSearchException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void shouldExecuteHybridSearch() {
        when(searchQueryService.search(any())).thenReturn(new SearchResponse(
                1,
                List.of(new SearchResult("ref-1", 12.0, Map.of("title", "water leak"), null)),
                List.of(),
                SearchPage.defaults(),
                20L));

        HybridSearchRequest request = new HybridSearchRequest(
                "CMP_COMPLAINT",
                ORG_ID,
                null,
                "water leak",
                null,
                null,
                SearchPage.defaults(),
                null,
                false,
                null,
                null,
                null,
                null);

        SearchResponse response = semanticSearchService.hybridSearch(request);

        assertThat(response.results()).isNotEmpty();
        assertThat(response.executionTimeMs()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void shouldExposeSemanticInfo() {
        SemanticSearchInfo info = semanticSearchService.getSemanticInfo();

        assertThat(info.provider()).isEqualTo("mock");
        assertThat(info.embeddingDimension()).isEqualTo(384);
        assertThat(info.semanticEnabled()).isTrue();
        assertThat(info.vectorIndexHealth()).isEqualTo(EmbeddingHealthStatus.UP);
        assertThat(info.indexedEmbeddingCount()).isGreaterThan(0);
    }

    private void seedEmbeddings() {
        SearchEmbedding embedding = new SearchEmbedding();
        embedding.setEmbeddingId(UUID.randomUUID());
        embedding.setReferenceId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        embedding.setOrganizationId(ORG_ID);
        embedding.setEntityType("COMPLAINT");
        embedding.setEmbeddingVersion(1);
        embedding.setVectorDimension(384);
        embedding.setVector(embeddingProvider.generateEmbedding("water leak complaint"));
        embedding.setCreatedDate(Instant.now());
        embedding.setUpdatedDate(Instant.now());
        vectorIndexService.indexEmbedding(embedding);
    }
}
