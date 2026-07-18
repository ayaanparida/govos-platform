package com.govos.srh.ai;

import com.govos.srh.ai.provider.EmbeddingCache;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.query.SearchPage;
import com.govos.srh.query.SearchQueryService;
import com.govos.srh.query.SearchRequest;
import com.govos.srh.query.SearchResponse;
import com.govos.srh.query.SearchResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SemanticSearchServiceImpl implements SemanticSearchService {

    private final EmbeddingProvider embeddingProvider;
    private final VectorIndexService vectorIndexService;
    private final HybridRankingService hybridRankingService;
    private final SearchQueryService searchQueryService;
    private final SearchProperties searchProperties;
    private final EmbeddingCache embeddingCache;

    public SemanticSearchServiceImpl(
            EmbeddingProvider embeddingProvider,
            VectorIndexService vectorIndexService,
            HybridRankingService hybridRankingService,
            SearchQueryService searchQueryService,
            SearchProperties searchProperties,
            EmbeddingCache embeddingCache) {
        this.embeddingProvider = embeddingProvider;
        this.vectorIndexService = vectorIndexService;
        this.hybridRankingService = hybridRankingService;
        this.searchQueryService = searchQueryService;
        this.searchProperties = searchProperties;
        this.embeddingCache = embeddingCache;
    }

    @Override
    public SearchResponse semanticSearch(SemanticSearchRequest request) {
        requireSemanticEnabled();
        validateRequest(request.queryText(), request.organizationId());

        long started = System.currentTimeMillis();
        SearchPage page = resolvePage(request.page());
        int topK = resolveTopK(request.topK(), page.size());

        float[] queryVector = embeddingProvider.generateEmbedding(request.queryText());
        List<VectorSearchHit> vectorHits = vectorIndexService.search(
                request.organizationId(), queryVector, topK);

        List<SearchResult> results = vectorHits.stream()
                .map(hit -> new SearchResult(
                        hit.id(),
                        hit.similarityScore(),
                        hit.metadata(),
                        null))
                .toList();

        long elapsed = System.currentTimeMillis() - started;
        return paginate(new SearchResponse(
                results.size(),
                results,
                List.of(),
                page,
                elapsed), page);
    }

    @Override
    public SearchResponse hybridSearch(HybridSearchRequest request) {
        requireSemanticEnabled();
        validateRequest(request.queryText(), request.organizationId());

        long started = System.currentTimeMillis();
        SearchPage page = resolvePage(request.page());
        int topK = resolveTopK(request.topK(), page.size());

        double keywordWeight = request.keywordWeight() != null
                ? request.keywordWeight()
                : searchProperties.getSemantic().getKeywordWeight();
        double semanticWeight = request.semanticWeight() != null
                ? request.semanticWeight()
                : searchProperties.getSemantic().getVectorWeight();
        validateWeights(keywordWeight, semanticWeight);

        SearchResponse keywordResponse = searchQueryService.search(new SearchRequest(
                request.indexCode(),
                request.organizationId(),
                request.userId(),
                request.queryText(),
                request.queryMode(),
                request.filters(),
                new SearchPage(0, Math.max(topK, page.size())),
                request.sort(),
                request.highlight(),
                request.facetFields()));

        float[] queryVector = embeddingProvider.generateEmbedding(request.queryText());
        List<VectorSearchHit> vectorHits = vectorIndexService.search(
                request.organizationId(), queryVector, topK);

        List<SearchResult> ranked = hybridRankingService.rank(
                keywordResponse.results(),
                vectorHits,
                keywordWeight,
                semanticWeight);

        long elapsed = System.currentTimeMillis() - started;
        return paginate(new SearchResponse(
                ranked.size(),
                ranked,
                keywordResponse.facets(),
                page,
                elapsed), page);
    }

    @Override
    public SemanticSearchInfo getSemanticInfo() {
        return new SemanticSearchInfo(
                embeddingProvider.providerName(),
                embeddingProvider.embeddingDimension(),
                searchProperties.getSemantic().isEnabled(),
                vectorIndexService.health(),
                embeddingProvider.health(),
                vectorIndexService.count(),
                resolveModelName(),
                searchProperties.getSemantic().getEmbeddingVersion(),
                embeddingCache.isEnabled() ? "UP" : "DISABLED",
                embeddingCache.size());
    }

    private String resolveModelName() {
        return switch (embeddingProvider.providerName()) {
            case "openai" -> searchProperties.getSemantic().getOpenai().getModel();
            case "azure-openai" -> searchProperties.getSemantic().getAzure().getDeployment();
            case "ollama" -> searchProperties.getSemantic().getOllama().getModel();
            default -> embeddingProvider.providerName();
        };
    }

    private void requireSemanticEnabled() {
        if (!searchProperties.getSemantic().isEnabled()) {
            throw new SemanticSearchException(
                    "Semantic search is disabled. Enable via govos.search.semantic.enabled=true");
        }
    }

    private static void validateRequest(String queryText, java.util.UUID organizationId) {
        if (queryText == null || queryText.isBlank()) {
            throw new SemanticSearchException("Query text is required for semantic search");
        }
        if (organizationId == null) {
            throw new SemanticSearchException("Organization id is required for semantic search");
        }
    }

    private static void validateWeights(double keywordWeight, double semanticWeight) {
        if (keywordWeight < 0D || semanticWeight < 0D) {
            throw new SemanticSearchException("Search weights must be non-negative");
        }
        if (keywordWeight + semanticWeight <= 0D) {
            throw new SemanticSearchException("At least one search weight must be greater than zero");
        }
    }

    private SearchPage resolvePage(SearchPage page) {
        if (page == null) {
            return SearchPage.defaults();
        }
        int size = Math.min(Math.max(page.size(), 1), SearchPage.MAX_SIZE);
        int resolvedPage = Math.max(page.page(), 0);
        return new SearchPage(resolvedPage, size);
    }

    private int resolveTopK(Integer requestTopK, int pageSize) {
        int configuredTopK = searchProperties.getSemantic().getTopK();
        int topK = requestTopK != null && requestTopK > 0 ? requestTopK : configuredTopK;
        return Math.max(topK, pageSize);
    }

    private static SearchResponse paginate(SearchResponse response, SearchPage page) {
        List<SearchResult> results = response.results();
        int fromIndex = page.offset();
        if (fromIndex >= results.size()) {
            return new SearchResponse(response.totalHits(), List.of(), response.facets(), page, response.executionTimeMs());
        }
        int toIndex = Math.min(fromIndex + page.size(), results.size());
        return new SearchResponse(
                response.totalHits(),
                results.subList(fromIndex, toIndex),
                response.facets(),
                page,
                response.executionTimeMs());
    }
}
