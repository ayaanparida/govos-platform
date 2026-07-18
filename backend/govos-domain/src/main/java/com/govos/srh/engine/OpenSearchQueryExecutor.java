package com.govos.srh.engine;

import com.govos.srh.exception.SearchEngineException;
import com.govos.srh.query.SearchFilters;
import com.govos.srh.query.SearchQueryMode;
import com.govos.srh.query.SearchSort;
import com.govos.srh.query.SortDirection;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.DistanceUnit;
import org.opensearch.client.opensearch._types.GeoLocation;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Highlight;
import org.opensearch.client.opensearch.core.search.HighlightField;
import org.opensearch.client.opensearch.core.search.Hit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class OpenSearchQueryExecutor {

    private static final List<String> DEFAULT_SEARCH_FIELDS = List.of("searchText", "title", "description");
    private static final List<String> HIGHLIGHT_FIELDS = List.of("searchText", "title", "description");
    private static final Map<String, String> FACET_FIELD_MAPPING = Map.of(
            "status", "status",
            "priority", "priority",
            "category", "categoryKey",
            "organization", "organizationId",
            "entityType", "entityType");

    private OpenSearchQueryExecutor() {
    }

    static EngineAdvancedSearchResult advancedSearch(OpenSearchClient client, EngineAdvancedSearchRequest request) {
        try {
            Query query = buildFilteredQuery(
                    request.organizationId(),
                    request.queryText(),
                    request.queryMode(),
                    request.filters(),
                    request.latitude(),
                    request.longitude(),
                    request.radiusKm(),
                    request.topLeftLatitude(),
                    request.topLeftLongitude(),
                    request.bottomRightLatitude(),
                    request.bottomRightLongitude());

            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index(request.indexName())
                    .from(request.from())
                    .size(request.size())
                    .query(query)
                    .timeout(request.timeoutMs() + "ms");

            applySort(builder, request.sort(), request.sortByDistance(), request.latitude(), request.longitude());
            applyHighlight(builder, request.highlight());
            applyFacetAggregations(builder, request.facetFields());

            var response = client.search(builder.build(), Map.class);
            return mapAdvancedResult(response.hits().total() != null ? response.hits().total().value() : 0,
                    response.hits().hits(),
                    response.aggregations());
        } catch (SearchEngineException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to execute advanced OpenSearch query on " + request.indexName(), ex);
        }
    }

    static List<String> autocomplete(OpenSearchClient client, EngineAutocompleteRequest request) {
        try {
            Query query = buildFilteredQuery(
                    request.organizationId(),
                    request.prefix(),
                    SearchQueryMode.PREFIX,
                    entityTypeFilter(request.entityType()),
                    null, null, null, null, null, null, null);

            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(request.indexName())
                    .size(request.limit())
                    .query(query)
                    .source(source -> source.filter(filter -> filter.includes("searchText")))
                    .timeout(request.timeoutMs() + "ms")
                    .build();

            var response = client.search(searchRequest, Map.class);
            Set<String> suggestions = new LinkedHashSet<>();
            if (response.hits().hits() != null) {
                for (Hit<Map> hit : response.hits().hits()) {
                    if (hit.source() == null) {
                        continue;
                    }
                    Object searchText = hit.source().get("searchText");
                    if (searchText != null) {
                        suggestions.add(searchText.toString());
                    }
                    if (suggestions.size() >= request.limit()) {
                        break;
                    }
                }
            }
            return new ArrayList<>(suggestions);
        } catch (SearchEngineException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to execute autocomplete on " + request.indexName(), ex);
        }
    }

    static long count(OpenSearchClient client, EngineCountRequest request) {
        try {
            Query query = buildFilteredQuery(
                    request.organizationId(),
                    request.queryText(),
                    request.queryMode(),
                    request.filters(),
                    null, null, null, null, null, null, null);

            CountRequest countRequest = new CountRequest.Builder()
                    .index(request.indexName())
                    .query(query)
                    .build();
            return client.count(countRequest).count();
        } catch (SearchEngineException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to count documents on " + request.indexName(), ex);
        }
    }

    static List<String> suggest(OpenSearchClient client, EngineSuggestRequest request) {
        return autocomplete(client, new EngineAutocompleteRequest(
                request.indexName(),
                request.organizationId(),
                request.prefix(),
                request.entityType(),
                request.limit(),
                request.timeoutMs()));
    }

    private static EngineAdvancedSearchResult mapAdvancedResult(
            long totalHits,
            List<Hit<Map>> hits,
            Map<String, Aggregate> aggregations) {
        List<EngineSearchHit> mappedHits = new ArrayList<>();
        if (hits != null) {
            for (Hit<Map> hit : hits) {
                Map<String, List<String>> highlights = new HashMap<>();
                if (hit.highlight() != null) {
                    hit.highlight().forEach((field, fragments) -> highlights.put(field, new ArrayList<>(fragments)));
                }
                mappedHits.add(new EngineSearchHit(
                        hit.id(),
                        hit.score(),
                        hit.source(),
                        highlights.isEmpty() ? null : highlights));
            }
        }

        List<EngineFacetResult> facets = new ArrayList<>();
        if (aggregations != null) {
            aggregations.forEach((name, aggregate) -> {
                if (aggregate.sterms() == null || aggregate.sterms().buckets().array() == null) {
                    return;
                }
                List<EngineFacetBucket> buckets = new ArrayList<>();
                for (StringTermsBucket bucket : aggregate.sterms().buckets().array()) {
                    buckets.add(new EngineFacetBucket(bucket.key(), bucket.docCount()));
                }
                facets.add(new EngineFacetResult(name, buckets));
            });
        }

        return new EngineAdvancedSearchResult(totalHits, mappedHits, facets);
    }

    private static void applySort(
            SearchRequest.Builder builder,
            List<SearchSort> sort,
            boolean sortByDistance,
            Double latitude,
            Double longitude) {
        if (sortByDistance && latitude != null && longitude != null) {
            builder.sort(s -> s.geoDistance(geo -> geo
                    .field("location")
                    .location(GeoLocation.of(loc -> loc.latlon(l -> l.lat(latitude).lon(longitude))))
                    .order(SortOrder.Asc)
                    .unit(DistanceUnit.Kilometers)));
            return;
        }
        if (sort == null || sort.isEmpty()) {
            builder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
            return;
        }
        for (SearchSort sortField : sort) {
            SortOrder order = sortField.direction() == SortDirection.ASC ? SortOrder.Asc : SortOrder.Desc;
            builder.sort(s -> s.field(f -> f.field(sortField.field()).order(order)));
        }
    }

    private static void applyHighlight(SearchRequest.Builder builder, boolean highlight) {
        if (!highlight) {
            return;
        }
        Map<String, HighlightField> fields = new LinkedHashMap<>();
        for (String field : HIGHLIGHT_FIELDS) {
            fields.put(field, HighlightField.of(h -> h));
        }
        builder.highlight(Highlight.of(h -> h.fields(fields)));
    }

    private static void applyFacetAggregations(SearchRequest.Builder builder, List<String> facetFields) {
        if (facetFields == null || facetFields.isEmpty()) {
            return;
        }
        Map<String, Aggregation> aggregations = new LinkedHashMap<>();
        for (String facetField : facetFields) {
            String mappedField = FACET_FIELD_MAPPING.getOrDefault(facetField, facetField);
            aggregations.put(facetField, Aggregation.of(a -> a.terms(t -> t.field(mappedField).size(50))));
        }
        builder.aggregations(aggregations);
    }

    private static Query buildFilteredQuery(
            UUID organizationId,
            String queryText,
            SearchQueryMode queryMode,
            SearchFilters filters,
            Double latitude,
            Double longitude,
            Double radiusKm,
            Double topLeftLatitude,
            Double topLeftLongitude,
            Double bottomRightLatitude,
            Double bottomRightLongitude) {
        List<Query> filterQueries = new ArrayList<>();
        filterQueries.add(termQuery("organizationId", organizationId.toString()));

        SearchFilters effectiveFilters = filters != null ? filters : entityTypeFilter(null);
        applyFilterQueries(filterQueries, effectiveFilters);
        applyDefaultVisibilityFilters(filterQueries, effectiveFilters);
        applyGeoFilters(filterQueries, latitude, longitude, radiusKm,
                topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude);

        Query textQuery = buildTextQuery(queryText, queryMode);
        return Query.of(q -> q.bool(b -> {
            BoolQuery.Builder bool = b.filter(filterQueries);
            if (textQuery != null) {
                bool.must(textQuery);
            }
            return bool;
        }));
    }

    private static SearchFilters entityTypeFilter(String entityType) {
        return entityType == null || entityType.isBlank()
                ? null
                : new SearchFilters(entityType, null, null, null, null,
                null, null, null, null, null, null);
    }

    private static void applyDefaultVisibilityFilters(List<Query> filterQueries, SearchFilters filters) {
        if (filters == null || filters.active() == null) {
            filterQueries.add(termQuery("active", "true"));
        }
        if (filters == null || filters.deleted() == null) {
            filterQueries.add(termQuery("deleted", "false"));
        }
    }

    private static void applyFilterQueries(List<Query> filterQueries, SearchFilters filters) {
        if (filters == null) {
            return;
        }
        addTermFilter(filterQueries, "entityType", filters.entityType());
        addTermFilter(filterQueries, "status", filters.status());
        addTermFilter(filterQueries, "priority", filters.priority());
        addTermFilter(filterQueries, "categoryKey", filters.category());
        addTermFilter(filterQueries, "subCategoryKey", filters.subCategory());
        addTermFilter(filterQueries, "active", filters.active());
        addTermFilter(filterQueries, "deleted", filters.deleted());
        addRangeFilter(filterQueries, "createdDate", filters.createdDateFrom(), filters.createdDateTo());
        addRangeFilter(filterQueries, "updatedDate", filters.updatedDateFrom(), filters.updatedDateTo());
    }

    private static void applyGeoFilters(
            List<Query> filterQueries,
            Double latitude,
            Double longitude,
            Double radiusKm,
            Double topLeftLatitude,
            Double topLeftLongitude,
            Double bottomRightLatitude,
            Double bottomRightLongitude) {
        if (latitude != null && longitude != null && radiusKm != null && radiusKm > 0) {
            filterQueries.add(Query.of(q -> q.geoDistance(g -> g
                    .field("location")
                    .location(GeoLocation.of(loc -> loc.latlon(l -> l.lat(latitude).lon(longitude))))
                    .distance(radiusKm + "km"))));
            return;
        }
        if (topLeftLatitude != null && topLeftLongitude != null
                && bottomRightLatitude != null && bottomRightLongitude != null) {
            filterQueries.add(Query.of(q -> q.geoBoundingBox(g -> g
                    .field("location")
                    .boundingBox(b -> b.tlbr(t -> t
                            .topLeft(GeoLocation.of(loc -> loc.latlon(l ->
                                    l.lat(topLeftLatitude).lon(topLeftLongitude))))
                            .bottomRight(GeoLocation.of(loc -> loc.latlon(l ->
                                    l.lat(bottomRightLatitude).lon(bottomRightLongitude)))))))));
        }
    }

    private static Query buildTextQuery(String queryText, SearchQueryMode queryMode) {
        if (queryText == null || queryText.isBlank()) {
            return Query.of(q -> q.matchAll(m -> m));
        }
        SearchQueryMode mode = queryMode != null ? queryMode : SearchQueryMode.FULL_TEXT;
        return switch (mode) {
            case PHRASE -> Query.of(q -> q.multiMatch(m -> m
                    .query(queryText)
                    .type(org.opensearch.client.opensearch._types.query_dsl.TextQueryType.Phrase)
                    .fields(DEFAULT_SEARCH_FIELDS)));
            case WILDCARD -> Query.of(q -> q.queryString(qs -> qs
                    .query("*" + escapeQueryString(queryText) + "*")
                    .fields(DEFAULT_SEARCH_FIELDS)));
            case PREFIX -> Query.of(q -> q.multiMatch(m -> m
                    .query(queryText)
                    .type(org.opensearch.client.opensearch._types.query_dsl.TextQueryType.PhrasePrefix)
                    .fields(DEFAULT_SEARCH_FIELDS)));
            case FUZZY -> Query.of(q -> q.multiMatch(m -> m
                    .query(queryText)
                    .fuzziness("AUTO")
                    .fields(DEFAULT_SEARCH_FIELDS)));
            case BOOLEAN -> Query.of(q -> q.queryString(qs -> qs
                    .query(queryText)
                    .fields(DEFAULT_SEARCH_FIELDS)));
            case FULL_TEXT -> Query.of(q -> q.multiMatch(m -> m
                    .query(queryText)
                    .fields(DEFAULT_SEARCH_FIELDS)));
        };
    }

    private static String escapeQueryString(String value) {
        return value.replace("\"", "\\\"");
    }

    private static Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t.field(field).value(v -> v.stringValue(value))));
    }

    private static Query termQuery(String field, boolean value) {
        return Query.of(q -> q.term(t -> t.field(field).value(v -> v.booleanValue(value))));
    }

    private static void addTermFilter(List<Query> filterQueries, String field, String value) {
        if (value != null && !value.isBlank()) {
            filterQueries.add(termQuery(field, value));
        }
    }

    private static void addTermFilter(List<Query> filterQueries, String field, Boolean value) {
        if (value != null) {
            filterQueries.add(termQuery(field, value));
        }
    }

    private static void addRangeFilter(
            List<Query> filterQueries,
            String field,
            Instant from,
            Instant to) {
        if (from == null && to == null) {
            return;
        }
        filterQueries.add(Query.of(q -> q.range(r -> {
            r.field(field);
            if (from != null) {
                r.gte(JsonData.of(from.toString()));
            }
            if (to != null) {
                r.lte(JsonData.of(to.toString()));
            }
            return r;
        })));
    }
}
