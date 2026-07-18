package com.govos.srh.production;

import com.govos.srh.ai.HybridSearchRequest;
import com.govos.srh.ai.SemanticSearchRequest;
import com.govos.srh.query.AutocompleteRequest;
import com.govos.srh.query.FacetSearchRequest;
import com.govos.srh.query.GeoSearchRequest;
import com.govos.srh.query.SearchPage;
import com.govos.srh.query.SearchRequest;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class SearchOperationAspect {

    private final SearchProductionGuard productionGuard;
    private final SearchMetricsRecorder metricsRecorder;
    private final SearchStructuredLogger structuredLogger;

    public SearchOperationAspect(
            SearchProductionGuard productionGuard,
            SearchMetricsRecorder metricsRecorder,
            SearchStructuredLogger structuredLogger) {
        this.productionGuard = productionGuard;
        this.metricsRecorder = metricsRecorder;
        this.structuredLogger = structuredLogger;
    }

    @Around("execution(* com.govos.srh.query.SearchQueryService.search(..)) && args(request)")
    public Object aroundSearch(ProceedingJoinPoint joinPoint, SearchRequest request) throws Throwable {
        productionGuard.validatePagination(resolvePage(request.page()));
        return execute("search", request.indexCode(), request.organizationId(), null, null,
                () -> metricsRecorder.recordSearchRequest("search"), joinPoint);
    }

    @Around("execution(* com.govos.srh.query.SearchQueryService.autocomplete(..)) && args(request)")
    public Object aroundAutocomplete(ProceedingJoinPoint joinPoint, AutocompleteRequest request) throws Throwable {
        return execute("autocomplete", request.indexCode(), request.organizationId(), request.entityType(), null,
                metricsRecorder::recordAutocompleteRequest, joinPoint);
    }

    @Around("execution(* com.govos.srh.query.SearchQueryService.facetSearch(..)) && args(request)")
    public Object aroundFacetSearch(ProceedingJoinPoint joinPoint, FacetSearchRequest request) throws Throwable {
        return execute("facet", request.indexCode(), request.organizationId(), null, null,
                metricsRecorder::recordFacetRequest, joinPoint);
    }

    @Around("execution(* com.govos.srh.query.SearchQueryService.geoSearch(..)) && args(request)")
    public Object aroundGeoSearch(ProceedingJoinPoint joinPoint, GeoSearchRequest request) throws Throwable {
        productionGuard.validatePagination(resolvePage(request.page()));
        return execute("geo", request.indexCode(), request.organizationId(), null, null,
                metricsRecorder::recordGeoRequest, joinPoint);
    }

    @Around("execution(* com.govos.srh.ai.SemanticSearchService.semanticSearch(..)) && args(request)")
    public Object aroundSemanticSearch(ProceedingJoinPoint joinPoint, SemanticSearchRequest request) throws Throwable {
        productionGuard.validatePagination(resolvePage(request.page()));
        return execute("semantic", request.indexCode(), request.organizationId(), null, null,
                metricsRecorder::recordSemanticRequest, joinPoint);
    }

    @Around("execution(* com.govos.srh.ai.SemanticSearchService.hybridSearch(..)) && args(request)")
    public Object aroundHybridSearch(ProceedingJoinPoint joinPoint, HybridSearchRequest request) throws Throwable {
        productionGuard.validatePagination(resolvePage(request.page()));
        return execute("hybrid", request.indexCode(), request.organizationId(), null, null,
                metricsRecorder::recordSemanticRequest, joinPoint);
    }

    private Object execute(
            String operation,
            String indexCode,
            UUID organizationId,
            String entityType,
            UUID referenceId,
            Runnable metricRecorder,
            ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = metricsRecorder.startTimer();
        long started = System.currentTimeMillis();
        metricRecorder.run();
        try {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - started;
            metricsRecorder.recordDuration(sample, "search.duration", operation);
            if ("semantic".equals(operation) || "hybrid".equals(operation)) {
                metricsRecorder.recordSemanticDuration(durationMs);
            }
            structuredLogger.logOperation(new SearchOperationContext(
                    operation, "SUCCESS", durationMs, organizationId, null, indexCode, entityType, referenceId));
            return result;
        } catch (Throwable ex) {
            long durationMs = System.currentTimeMillis() - started;
            metricsRecorder.recordSearchError(operation);
            structuredLogger.logOperation(new SearchOperationContext(
                    operation, "ERROR", durationMs, organizationId, null, indexCode, entityType, referenceId));
            throw ex;
        }
    }

    private static SearchPage resolvePage(SearchPage page) {
        return page != null ? page : SearchPage.defaults();
    }
}
