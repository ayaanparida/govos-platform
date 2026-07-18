package com.govos.srh.observability;

import com.govos.srh.ai.HybridSearchRequest;
import com.govos.srh.ai.SemanticSearchRequest;
import com.govos.srh.ai.job.EmbeddingGenerationService;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.engine.BulkOperationResult;
import com.govos.srh.query.AutocompleteRequest;
import com.govos.srh.query.FacetSearchRequest;
import com.govos.srh.query.GeoSearchRequest;
import com.govos.srh.production.SearchOperationalHealthService;
import com.govos.srh.query.SearchRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@ConditionalOnObservationEnabled
public class SearchObservationAspect {

    private final SearchOperationTracer operationTracer;
    private final SearchProperties searchProperties;

    public SearchObservationAspect(SearchOperationTracer operationTracer, SearchProperties searchProperties) {
        this.operationTracer = operationTracer;
        this.searchProperties = searchProperties;
    }

    @Around("execution(* com.govos.srh.query.SearchQueryService.search(..)) && args(request)")
    public Object aroundSearch(ProceedingJoinPoint joinPoint, SearchRequest request) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.SEARCH_QUERY,
                attributes("search.query", request.organizationId(), 0L, null),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.query.SearchQueryService.autocomplete(..)) && args(request)")
    public Object aroundAutocomplete(ProceedingJoinPoint joinPoint, AutocompleteRequest request) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.AUTOCOMPLETE,
                attributes("search.autocomplete", request.organizationId(), 0L, null),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.query.SearchQueryService.facetSearch(..)) && args(request)")
    public Object aroundFacetSearch(ProceedingJoinPoint joinPoint, FacetSearchRequest request) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.FACET_SEARCH,
                attributes("search.facet", request.organizationId(), 0L, null),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.query.SearchQueryService.geoSearch(..)) && args(request)")
    public Object aroundGeoSearch(ProceedingJoinPoint joinPoint, GeoSearchRequest request) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.GEO_SEARCH,
                attributes("search.geo", request.organizationId(), 0L, null),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.ai.SemanticSearchService.semanticSearch(..)) && args(request)")
    public Object aroundSemanticSearch(ProceedingJoinPoint joinPoint, SemanticSearchRequest request) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.SEMANTIC_SEARCH,
                semanticAttributes("search.semantic", request.organizationId()),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.ai.SemanticSearchService.hybridSearch(..)) && args(request)")
    public Object aroundHybridSearch(ProceedingJoinPoint joinPoint, HybridSearchRequest request) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.HYBRID_SEARCH,
                semanticAttributes("search.hybrid", request.organizationId()),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.ai.job.EmbeddingGenerationService.startJob(..))")
    public Object aroundEmbeddingGeneration(ProceedingJoinPoint joinPoint) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.EMBEDDING_GENERATION,
                new SearchOperationTracer.TraceContextAttributes(
                        "search.embedding.generation",
                        null,
                        0L,
                        searchProperties.getSemantic().getProvider(),
                        "opensearch",
                        null,
                        SearchObservationEventType.SEMANTIC_SEARCH_COMPLETED,
                        SearchObservationEventType.SEARCH_FAILED),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.ai.VectorIndexService.indexEmbedding(..)) || "
            + "execution(* com.govos.srh.ai.VectorIndexService.indexEmbeddings(..))")
    public Object aroundVectorIndexing(ProceedingJoinPoint joinPoint) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.VECTOR_INDEXING,
                attributes("search.vector.index", null, 0L, searchProperties.getSemantic().getProvider()),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.service.SearchIndexService.bulkIndex(..))")
    public Object aroundBulkIndex(ProceedingJoinPoint joinPoint) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.BULK_INDEXING,
                new SearchOperationTracer.TraceContextAttributes(
                        "search.bulk.index",
                        null,
                        0L,
                        null,
                        "opensearch",
                        SearchObservationEventType.BULK_INDEX_STARTED,
                        SearchObservationEventType.BULK_INDEX_COMPLETED,
                        SearchObservationEventType.SEARCH_FAILED),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.service.SearchIndexService.switchAlias(..))")
    public Object aroundAliasSwitch(ProceedingJoinPoint joinPoint) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.ALIAS_SWITCH,
                attributes("search.alias.switch", null, 0L, null),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.admin.SearchAdministrationService.reindexIndex(..)) || "
            + "execution(* com.govos.srh.admin.SearchAdministrationService.reindexAll(..))")
    public Object aroundReindex(ProceedingJoinPoint joinPoint) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.REINDEX,
                attributes("search.reindex", null, 0L, null),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.scheduler.SearchSchedulerService.run*(..)) || "
            + "execution(* com.govos.srh.scheduler.SearchSchedulerService.trigger*(..))")
    public Object aroundScheduler(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = joinPoint.getSignature().getName();
        return operationTracer.trace(
                SearchSpanNames.SCHEDULER_EXECUTION,
                new SearchOperationTracer.TraceContextAttributes(
                        operation,
                        null,
                        0L,
                        null,
                        "opensearch",
                        SearchObservationEventType.SCHEDULER_STARTED,
                        SearchObservationEventType.SCHEDULER_COMPLETED,
                        SearchObservationEventType.SEARCH_FAILED),
                () -> proceed(joinPoint));
    }

    @Around("execution(* com.govos.srh.production.SearchOperationalHealthService.getOperationalHealth(..))")
    public Object aroundClusterHealth(ProceedingJoinPoint joinPoint) throws Throwable {
        return operationTracer.trace(
                SearchSpanNames.CLUSTER_HEALTH,
                new SearchOperationTracer.TraceContextAttributes(
                        "search.cluster.health",
                        null,
                        0L,
                        searchProperties.getSemantic().getProvider(),
                        "opensearch",
                        null,
                        SearchObservationEventType.CLUSTER_HEALTH_CHECKED,
                        null),
                () -> proceed(joinPoint));
    }

    private SearchOperationTracer.TraceContextAttributes attributes(
            String operation,
            UUID organizationId,
            long documentCount,
            String provider) {
        return new SearchOperationTracer.TraceContextAttributes(
                operation,
                organizationId,
                documentCount,
                provider,
                "opensearch",
                SearchObservationEventType.SEARCH_STARTED,
                SearchObservationEventType.SEARCH_COMPLETED,
                SearchObservationEventType.SEARCH_FAILED);
    }

    private SearchOperationTracer.TraceContextAttributes semanticAttributes(String operation, UUID organizationId) {
        return new SearchOperationTracer.TraceContextAttributes(
                operation,
                organizationId,
                0L,
                searchProperties.getSemantic().getProvider(),
                "opensearch",
                SearchObservationEventType.SEMANTIC_SEARCH_STARTED,
                SearchObservationEventType.SEMANTIC_SEARCH_COMPLETED,
                SearchObservationEventType.SEARCH_FAILED);
    }

    private static Object proceed(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new SearchObservationException("Observation join point failed", ex);
        }
    }
}
