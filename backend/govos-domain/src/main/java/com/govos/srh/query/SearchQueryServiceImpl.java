package com.govos.srh.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.engine.EngineAdvancedSearchRequest;
import com.govos.srh.engine.EngineAdvancedSearchResult;
import com.govos.srh.engine.EngineAutocompleteRequest;
import com.govos.srh.engine.EngineCountRequest;
import com.govos.srh.engine.EngineFacetBucket;
import com.govos.srh.engine.EngineFacetResult;
import com.govos.srh.engine.EngineSearchHit;
import com.govos.srh.engine.EngineSuggestRequest;
import com.govos.srh.engine.SearchEngineProvider;
import com.govos.srh.enums.SearchQueryType;
import com.govos.srh.exception.SearchException;
import com.govos.srh.service.SearchQueryHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchQueryServiceImpl implements SearchQueryService {

    private final SearchEngineProvider searchEngineProvider;
    private final SearchIndexReadTargetResolver readTargetResolver;
    private final SearchQueryHistoryService searchQueryHistoryService;
    private final SearchQueryValidator searchQueryValidator;
    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper;

    public SearchQueryServiceImpl(
            SearchEngineProvider searchEngineProvider,
            SearchIndexReadTargetResolver readTargetResolver,
            SearchQueryHistoryService searchQueryHistoryService,
            SearchQueryValidator searchQueryValidator,
            SearchProperties searchProperties,
            ObjectMapper objectMapper) {
        this.searchEngineProvider = searchEngineProvider;
        this.readTargetResolver = readTargetResolver;
        this.searchQueryHistoryService = searchQueryHistoryService;
        this.searchQueryValidator = searchQueryValidator;
        this.searchProperties = searchProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        searchQueryValidator.validateSearch(request);
        SearchPage page = searchQueryValidator.resolvePage(request.page());
        long started = System.currentTimeMillis();

        EngineAdvancedSearchResult engineResult = executeAdvancedSearch(
                request.indexCode(),
                request.organizationId(),
                request.queryText(),
                request.queryMode(),
                request.filters(),
                page,
                request.sort(),
                Boolean.TRUE.equals(request.highlight()),
                searchQueryValidator.resolveFacetFields(request.facetFields()),
                null, null, null, null, null, null, null, false);

        long elapsed = System.currentTimeMillis() - started;
        SearchResponse response = mapResponse(engineResult, page, elapsed);
        recordHistory(request.organizationId(), request.userId(), request.queryText(),
                SearchQueryType.SEARCH, request.filters(), response.totalHits(), elapsed);
        return response;
    }

    @Override
    public AutocompleteResponse autocomplete(AutocompleteRequest request) {
        searchQueryValidator.validateAutocomplete(request);
        long started = System.currentTimeMillis();
        String indexName = readTargetResolver.resolveReadTarget(request.indexCode());
        int limit = searchQueryValidator.resolveAutocompleteLimit(request.limit());

        List<String> suggestions = searchEngineProvider.autocomplete(new EngineAutocompleteRequest(
                indexName,
                request.organizationId(),
                request.prefix(),
                request.entityType(),
                limit,
                searchProperties.getQueryTimeoutMs()));

        long elapsed = System.currentTimeMillis() - started;
        recordHistory(request.organizationId(), request.userId(), request.prefix(),
                SearchQueryType.AUTOCOMPLETE, entityTypeFilters(request.entityType()),
                suggestions.size(), elapsed);
        return new AutocompleteResponse(suggestions, elapsed);
    }

    @Override
    public SearchResponse geoSearch(GeoSearchRequest request) {
        searchQueryValidator.validateGeoSearch(request);
        SearchPage page = searchQueryValidator.resolvePage(request.page());
        long started = System.currentTimeMillis();

        EngineAdvancedSearchResult engineResult = executeAdvancedSearch(
                request.indexCode(),
                request.organizationId(),
                request.queryText(),
                SearchQueryMode.FULL_TEXT,
                request.filters(),
                page,
                List.of(),
                false,
                List.of(),
                toDouble(request.latitude()),
                toDouble(request.longitude()),
                request.radiusKm(),
                toDouble(request.topLeftLatitude()),
                toDouble(request.topLeftLongitude()),
                toDouble(request.bottomRightLatitude()),
                toDouble(request.bottomRightLongitude()),
                Boolean.TRUE.equals(request.sortByDistance()));

        long elapsed = System.currentTimeMillis() - started;
        SearchResponse response = mapResponse(engineResult, page, elapsed);
        recordHistory(request.organizationId(), request.userId(), request.queryText(),
                SearchQueryType.GEO, request.filters(), response.totalHits(), elapsed);
        return response;
    }

    @Override
    public SearchResponse facetSearch(FacetSearchRequest request) {
        searchQueryValidator.validateFacetSearch(request);
        SearchPage page = new SearchPage(SearchPage.DEFAULT_PAGE, 0);
        long started = System.currentTimeMillis();

        EngineAdvancedSearchResult engineResult = executeAdvancedSearch(
                request.indexCode(),
                request.organizationId(),
                request.queryText(),
                SearchQueryMode.FULL_TEXT,
                request.filters(),
                page,
                List.of(),
                false,
                searchQueryValidator.resolveFacetFields(request.facetFields()),
                null, null, null, null, null, null, null, false);

        long elapsed = System.currentTimeMillis() - started;
        SearchResponse response = new SearchResponse(
                engineResult.totalHits(),
                List.of(),
                mapFacets(engineResult.facets()),
                page,
                elapsed);
        recordHistory(request.organizationId(), request.userId(), request.queryText(),
                SearchQueryType.FACET, request.filters(), engineResult.totalHits(), elapsed);
        return response;
    }

    @Override
    public long count(SearchRequest request) {
        searchQueryValidator.validateSearch(request);
        long started = System.currentTimeMillis();
        String indexName = readTargetResolver.resolveReadTarget(request.indexCode());

        long total = searchEngineProvider.countDocuments(new EngineCountRequest(
                indexName,
                request.organizationId(),
                request.queryText(),
                request.queryMode(),
                request.filters(),
                searchProperties.getQueryTimeoutMs()));

        long elapsed = System.currentTimeMillis() - started;
        recordHistory(request.organizationId(), request.userId(), request.queryText(),
                SearchQueryType.SEARCH, request.filters(), total, elapsed);
        return total;
    }

    @Override
    public List<String> suggest(AutocompleteRequest request) {
        searchQueryValidator.validateAutocomplete(request);
        String indexName = readTargetResolver.resolveReadTarget(request.indexCode());
        int limit = searchQueryValidator.resolveAutocompleteLimit(request.limit());

        return searchEngineProvider.suggest(new EngineSuggestRequest(
                indexName,
                request.organizationId(),
                request.prefix(),
                request.entityType(),
                limit,
                searchProperties.getQueryTimeoutMs()));
    }

    private EngineAdvancedSearchResult executeAdvancedSearch(
            String indexCode,
            java.util.UUID organizationId,
            String queryText,
            SearchQueryMode queryMode,
            SearchFilters filters,
            SearchPage page,
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
            boolean sortByDistance) {
        String indexName = readTargetResolver.resolveReadTarget(indexCode);
        return searchEngineProvider.advancedSearch(new EngineAdvancedSearchRequest(
                indexName,
                organizationId,
                queryText,
                queryMode,
                filters,
                page.offset(),
                page.size(),
                sort,
                highlight,
                facetFields,
                latitude,
                longitude,
                radiusKm,
                topLeftLatitude,
                topLeftLongitude,
                bottomRightLatitude,
                bottomRightLongitude,
                sortByDistance,
                searchProperties.getQueryTimeoutMs()));
    }

    private SearchResponse mapResponse(
            EngineAdvancedSearchResult engineResult,
            SearchPage page,
            long elapsed) {
        List<SearchResult> results = engineResult.hits().stream()
                .map(this::mapHit)
                .toList();
        return new SearchResponse(
                engineResult.totalHits(),
                results,
                mapFacets(engineResult.facets()),
                page,
                elapsed);
    }

    private SearchResult mapHit(EngineSearchHit hit) {
        return new SearchResult(hit.id(), hit.score(), hit.source(), hit.highlights());
    }

    private List<FacetResult> mapFacets(List<EngineFacetResult> facets) {
        if (facets == null) {
            return List.of();
        }
        return facets.stream()
                .map(facet -> new FacetResult(
                        facet.name(),
                        facet.buckets().stream()
                                .map(this::mapFacetBucket)
                                .toList()))
                .toList();
    }

    private FacetBucket mapFacetBucket(EngineFacetBucket bucket) {
        return new FacetBucket(bucket.key(), bucket.count());
    }

    private void recordHistory(
            java.util.UUID organizationId,
            java.util.UUID userId,
            String queryText,
            SearchQueryType queryType,
            SearchFilters filters,
            long resultCount,
            long executionTimeMs) {
        searchQueryHistoryService.record(new SearchQueryHistoryDto(
                null,
                null,
                organizationId,
                userId,
                queryText,
                queryType,
                serializeFilters(filters),
                resultCount,
                executionTimeMs,
                Instant.now(),
                true,
                null,
                null,
                null,
                null,
                null));
    }

    private String serializeFilters(SearchFilters filters) {
        if (filters == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(filters);
        } catch (JsonProcessingException ex) {
            throw new SearchException("Failed to serialize search filters for query history", ex);
        }
    }

    private SearchFilters entityTypeFilters(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            return null;
        }
        return new SearchFilters(entityType, null, null, null, null,
                null, null, null, null, null, null);
    }

    private Double toDouble(java.math.BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}
