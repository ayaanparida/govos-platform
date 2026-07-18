package com.govos.api.srh.controller;

import com.govos.api.common.pagination.PageMapper;
import com.govos.api.common.pagination.PageResponse;
import com.govos.api.common.pagination.SortParser;
import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.api.common.validation.PaginationRequest;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.srh.admin.SearchClusterInfoDto;
import com.govos.srh.admin.SearchDashboardDto;
import com.govos.srh.admin.SearchHealthDto;
import com.govos.srh.admin.SemanticProviderInfoDto;
import com.govos.srh.observability.SearchErrorSnapshotDto;
import com.govos.srh.observability.SearchLatencySnapshotDto;
import com.govos.srh.observability.SearchMetricsSnapshotDto;
import com.govos.srh.observability.SearchObservabilitySnapshotDto;
import com.govos.srh.observability.SearchObservationEvent;
import com.govos.srh.observability.SearchTraceRecord;
import com.govos.srh.scheduler.SearchScheduledJobRecordDto;
import com.govos.srh.scheduler.SearchSchedulerStatusDto;
import com.govos.srh.admin.SearchIndexStatisticsDto;
import com.govos.srh.admin.SearchQueryStatisticsDto;
import com.govos.srh.admin.SearchSlowQueryDto;
import com.govos.srh.admin.SearchStatisticsDto;
import com.govos.srh.admin.SearchTopQueryDto;
import com.govos.srh.ai.HybridSearchRequest;
import com.govos.srh.ai.SemanticSearchRequest;
import com.govos.srh.dto.SearchAliasCreateRequest;
import com.govos.srh.dto.SearchAliasDto;
import com.govos.srh.dto.SearchAliasUpdateRequest;
import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.dto.SearchIndexUpdateRequest;
import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.dto.SearchSyncJobUpdateRequest;
import com.govos.srh.production.SearchOperationalHealthDto;
import com.govos.srh.query.AutocompleteRequest;
import com.govos.srh.query.AutocompleteResponse;
import com.govos.srh.query.FacetSearchRequest;
import com.govos.srh.query.GeoSearchRequest;
import com.govos.srh.query.SearchRequest;
import com.govos.srh.query.SearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/search")
@Validated
@Tag(name = "Search", description = "Search administration and query execution")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final SearchApplicationService searchApplicationService;

    public SearchController(SearchApplicationService searchApplicationService) {
        this.searchApplicationService = searchApplicationService;
    }

    // --- Search Index ---

    @PostMapping("/indexes")
    @Operation(summary = "Create search index")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Search index created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<SearchIndexDto>> createIndex(
            @Valid @RequestBody SearchIndexCreateRequest request,
            HttpServletRequest httpRequest) {
        SearchIndexDto created = searchApplicationService.createIndex(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Search index created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/indexes/{id}")
    @Operation(summary = "Update search index")
    public ApiResponse<SearchIndexDto> updateIndex(
            @Parameter(description = "Search index identifier") @PathVariable UUID id,
            @Valid @RequestBody SearchIndexUpdateRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.updateIndex(id, request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/indexes/{id}")
    @Operation(summary = "Get search index by id")
    public ApiResponse<SearchIndexDto> getIndex(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getIndex(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/indexes/code/{code}")
    @Operation(summary = "Get search index by code")
    public ApiResponse<SearchIndexDto> getIndexByCode(
            @PathVariable String code,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getIndexByCode(code),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/indexes")
    @Operation(summary = "List search indexes")
    public ApiResponse<PageResponse<SearchIndexDto>> listIndexes(
            @Valid PaginationRequest pagination,
            HttpServletRequest httpRequest) {
        var pageable = PageRequest.of(
                pagination.page(),
                pagination.size(),
                SortParser.parse(pagination.sort()));
        return ApiResponse.ok(
                PageMapper.toPageResponse(searchApplicationService.listIndexes(pageable)),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/indexes/{id}/activate")
    @Operation(summary = "Activate search index")
    public ApiResponse<SearchIndexDto> activateIndex(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.activateIndex(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/indexes/{id}/archive")
    @Operation(summary = "Archive search index")
    public ApiResponse<SearchIndexDto> archiveIndex(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.archiveIndex(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @DeleteMapping("/indexes/{id}")
    @Operation(summary = "Soft delete search index")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Search index deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Search index not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> softDeleteIndex(@PathVariable UUID id) {
        searchApplicationService.softDeleteIndex(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/indexes/{id}/restore")
    @Operation(summary = "Restore soft-deleted search index")
    public ApiResponse<SearchIndexDto> restoreIndex(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.restoreIndex(id),
                "Search index restored",
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Search Documents ---

    @PostMapping("/documents")
    @Operation(summary = "Create search document")
    public ResponseEntity<ApiResponse<SearchDocumentDto>> createDocument(
            @Valid @RequestBody SearchDocumentCreateRequest request,
            HttpServletRequest httpRequest) {
        SearchDocumentDto created = searchApplicationService.createDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Search document created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/documents/{id}")
    @Operation(summary = "Update search document")
    public ApiResponse<SearchDocumentDto> updateDocument(
            @PathVariable UUID id,
            @Valid @RequestBody SearchDocumentUpdateRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.updateDocument(id, request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/documents/{id}")
    @Operation(summary = "Get search document by id")
    public ApiResponse<SearchDocumentDto> getDocument(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getDocument(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/documents/index/{indexId}")
    @Operation(summary = "List search documents by index")
    public ApiResponse<List<SearchDocumentDto>> listDocumentsByIndex(
            @PathVariable UUID indexId,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.listDocumentsByIndex(indexId),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/documents/organization/{organizationId}")
    @Operation(summary = "List search documents by organization")
    public ApiResponse<List<SearchDocumentDto>> listDocumentsByOrganization(
            @PathVariable UUID organizationId,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.listDocumentsByOrganization(organizationId),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @DeleteMapping("/documents/{id}")
    @Operation(summary = "Soft delete search document")
    public ResponseEntity<Void> softDeleteDocument(@PathVariable UUID id) {
        searchApplicationService.softDeleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/documents/{id}/restore")
    @Operation(summary = "Restore soft-deleted search document")
    public ApiResponse<SearchDocumentDto> restoreDocument(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.restoreDocument(id),
                "Search document restored",
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Search Alias ---

    @PostMapping("/aliases")
    @Operation(summary = "Create search alias")
    public ResponseEntity<ApiResponse<SearchAliasDto>> createAlias(
            @Valid @RequestBody SearchAliasCreateRequest request,
            HttpServletRequest httpRequest) {
        SearchAliasDto created = searchApplicationService.createAlias(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Search alias created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/aliases/{id}")
    @Operation(summary = "Update search alias")
    public ApiResponse<SearchAliasDto> updateAlias(
            @PathVariable UUID id,
            @Valid @RequestBody SearchAliasUpdateRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.updateAlias(id, request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/aliases/{aliasName}")
    @Operation(summary = "Get search alias by name")
    public ApiResponse<SearchAliasDto> getAlias(
            @PathVariable String aliasName,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getAlias(aliasName),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/aliases/index/{indexId}")
    @Operation(summary = "List search aliases by index")
    public ApiResponse<List<SearchAliasDto>> listAliases(
            @PathVariable UUID indexId,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.listAliases(indexId),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/aliases/{id}/activate")
    @Operation(summary = "Activate search alias")
    public ApiResponse<SearchAliasDto> activateAlias(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.activateAlias(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @DeleteMapping("/aliases/{id}")
    @Operation(summary = "Soft delete search alias")
    public ResponseEntity<Void> softDeleteAlias(@PathVariable UUID id) {
        searchApplicationService.softDeleteAlias(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/aliases/{id}/restore")
    @Operation(summary = "Restore soft-deleted search alias")
    public ApiResponse<SearchAliasDto> restoreAlias(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.restoreAlias(id),
                "Search alias restored",
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Search Sync Job ---

    @PostMapping("/jobs")
    @Operation(summary = "Create search sync job")
    public ResponseEntity<ApiResponse<SearchSyncJobDto>> createJob(
            @Valid @RequestBody SearchSyncJobCreateRequest request,
            HttpServletRequest httpRequest) {
        SearchSyncJobDto created = searchApplicationService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Search sync job created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/jobs/{id}")
    @Operation(summary = "Update search sync job")
    public ApiResponse<SearchSyncJobDto> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody SearchSyncJobUpdateRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.updateJob(id, request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Get search sync job by id")
    public ApiResponse<SearchSyncJobDto> getJob(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getJob(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/jobs/index/{indexId}")
    @Operation(summary = "List search sync jobs by index")
    public ApiResponse<List<SearchSyncJobDto>> listJobs(
            @PathVariable UUID indexId,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.listJobs(indexId),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/jobs/{id}/start")
    @Operation(summary = "Start search sync job")
    public ApiResponse<SearchSyncJobDto> startJob(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.startJob(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/jobs/{id}/complete")
    @Operation(summary = "Complete search sync job")
    public ApiResponse<SearchSyncJobDto> completeJob(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.completeJob(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/jobs/{id}/fail")
    @Operation(summary = "Fail search sync job")
    public ApiResponse<SearchSyncJobDto> failJob(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.failJob(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/jobs/{id}/cancel")
    @Operation(summary = "Cancel search sync job")
    public ApiResponse<SearchSyncJobDto> cancelJob(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.cancelJob(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Query History ---

    @GetMapping("/queries/organization/{organizationId}")
    @Operation(summary = "List query history by organization")
    public ApiResponse<List<SearchQueryHistoryDto>> listQueriesByOrganization(
            @PathVariable UUID organizationId,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.listQueriesByOrganization(organizationId),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/queries/user/{userId}")
    @Operation(summary = "List query history by user")
    public ApiResponse<List<SearchQueryHistoryDto>> listQueriesByUser(
            @PathVariable UUID userId,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.listQueriesByUser(userId),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Query Execution (SRH-014) ---

    @PostMapping("/query")
    @Operation(summary = "Execute full-text search query")
    public ApiResponse<SearchResponse> search(
            @Valid @RequestBody SearchRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.search(request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/autocomplete")
    @Operation(summary = "Execute autocomplete query")
    public ApiResponse<AutocompleteResponse> autocomplete(
            @Valid @RequestBody AutocompleteRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.autocomplete(request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/facets")
    @Operation(summary = "Execute facet aggregation query")
    public ApiResponse<SearchResponse> facetSearch(
            @Valid @RequestBody FacetSearchRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.facetSearch(request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/geo")
    @Operation(summary = "Execute geo search query")
    public ApiResponse<SearchResponse> geoSearch(
            @Valid @RequestBody GeoSearchRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.geoSearch(request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/count")
    @Operation(summary = "Count matching search documents")
    public ApiResponse<Long> count(
            @RequestParam String indexCode,
            @RequestParam UUID organizationId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String queryText,
            HttpServletRequest httpRequest) {
        SearchRequest request = new SearchRequest(
                indexCode,
                organizationId,
                userId,
                queryText,
                null,
                null,
                null,
                null,
                false,
                null);
        return ApiResponse.ok(
                searchApplicationService.count(request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Semantic Search (SRH-016) ---

    @PostMapping("/semantic")
    @Operation(summary = "Execute semantic (vector) search",
            description = "Requires govos.search.semantic.enabled=true. Keyword search remains the default path via POST /query.")
    public ApiResponse<SearchResponse> semanticSearch(
            @Valid @RequestBody SemanticSearchRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.semanticSearch(request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/hybrid")
    @Operation(summary = "Execute hybrid keyword + semantic search",
            description = "Combines BM25 keyword relevance with vector similarity using configurable weights.")
    public ApiResponse<SearchResponse> hybridSearch(
            @Valid @RequestBody HybridSearchRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.hybridSearch(request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Administration (SRH-015 / SRH-017) ---

    @GetMapping("/admin/health")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get search cluster health", description = "Requires SRH_MONITOR permission")
    public ApiResponse<SearchHealthDto> getClusterHealth(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getClusterHealth(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/health/operational")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get production operational health", description = "Requires SRH_MONITOR permission")
    public ApiResponse<SearchOperationalHealthDto> getOperationalHealth(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getOperationalHealth(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/semantic/provider")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get semantic embedding provider information", description = "Requires SRH_MONITOR permission")
    public ApiResponse<SemanticProviderInfoDto> getSemanticProviderInfo(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getSemanticProviderInfo(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/cluster")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get OpenSearch cluster information", description = "Requires SRH_MONITOR permission")
    public ApiResponse<SearchClusterInfoDto> getClusterInformation(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getClusterInformation(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Get platform search statistics", description = "Requires SRH_ADMIN permission")
    public ApiResponse<SearchStatisticsDto> getSearchStatistics(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getSearchStatistics(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Get search administration dashboard", description = "Requires SRH_ADMIN permission")
    public ApiResponse<SearchDashboardDto> getSearchDashboard(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getSearchDashboard(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/indexes/{id}/statistics")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Get search index statistics", description = "Requires SRH_ADMIN permission")
    public ApiResponse<SearchIndexStatisticsDto> getIndexStatistics(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getIndexStatistics(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/queries/top")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Get top search queries", description = "Requires SRH_ADMIN permission")
    public ApiResponse<List<SearchTopQueryDto>> getTopQueries(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getTopQueries(limit),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/queries/slow")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Get slowest search queries", description = "Requires SRH_ADMIN permission")
    public ApiResponse<List<SearchSlowQueryDto>> getSlowQueries(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getSlowQueries(limit),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/admin/indexes/{id}/reindex")
    @PreAuthorize("hasAuthority('SRH_REINDEX')")
    @Operation(summary = "Reindex a single search index", description = "Requires SRH_REINDEX permission")
    public ResponseEntity<ApiResponse<SearchSyncJobDto>> reindexIndex(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        SearchSyncJobDto job = searchApplicationService.reindexIndex(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.ok(job, "Reindex started", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PostMapping("/admin/reindex-all")
    @PreAuthorize("hasAuthority('SRH_REINDEX')")
    @Operation(summary = "Reindex all active search indexes", description = "Requires SRH_REINDEX permission")
    public ResponseEntity<ApiResponse<List<SearchSyncJobDto>>> reindexAll(HttpServletRequest httpRequest) {
        List<SearchSyncJobDto> jobs = searchApplicationService.reindexAll();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.ok(jobs, "Platform reindex started", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PostMapping("/admin/jobs/{id}/cancel")
    @PreAuthorize("hasAuthority('SRH_REINDEX')")
    @Operation(summary = "Cancel a running reindex job", description = "Requires SRH_REINDEX permission")
    public ApiResponse<SearchSyncJobDto> cancelReindex(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.cancelReindex(id),
                "Reindex job cancelled",
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Scheduler (SRH-019) ---

    @GetMapping("/admin/scheduler")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Get scheduler status", description = "Requires SRH_ADMIN permission")
    public ApiResponse<SearchSchedulerStatusDto> getSchedulerStatus(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getSchedulerStatus(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/admin/scheduler/reindex")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Trigger scheduler reindex", description = "Requires SRH_ADMIN permission")
    public ResponseEntity<ApiResponse<SearchScheduledJobRecordDto>> triggerSchedulerReindex(
            @RequestParam(defaultValue = "true") boolean full,
            HttpServletRequest httpRequest) {
        SearchScheduledJobRecordDto record = searchApplicationService.triggerSchedulerReindex(full);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.ok(record, "Scheduler reindex triggered", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PostMapping("/admin/scheduler/embedding")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Trigger scheduler embedding generation", description = "Requires SRH_ADMIN permission")
    public ResponseEntity<ApiResponse<SearchScheduledJobRecordDto>> triggerSchedulerEmbedding(
            HttpServletRequest httpRequest) {
        SearchScheduledJobRecordDto record = searchApplicationService.triggerSchedulerEmbedding();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.ok(record, "Scheduler embedding triggered", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PostMapping("/admin/scheduler/cache")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Trigger scheduler cache maintenance", description = "Requires SRH_ADMIN permission")
    public ResponseEntity<ApiResponse<SearchScheduledJobRecordDto>> triggerSchedulerCache(
            HttpServletRequest httpRequest) {
        SearchScheduledJobRecordDto record = searchApplicationService.triggerSchedulerCache();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.ok(record, "Scheduler cache maintenance triggered", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PostMapping("/admin/scheduler/statistics")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Trigger scheduler statistics refresh", description = "Requires SRH_ADMIN permission")
    public ResponseEntity<ApiResponse<SearchScheduledJobRecordDto>> triggerSchedulerStatistics(
            HttpServletRequest httpRequest) {
        SearchScheduledJobRecordDto record = searchApplicationService.triggerSchedulerStatistics();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.ok(record, "Scheduler statistics refresh triggered", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PostMapping("/admin/scheduler/cleanup")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Trigger scheduler cleanup", description = "Requires SRH_ADMIN permission")
    public ResponseEntity<ApiResponse<SearchScheduledJobRecordDto>> triggerSchedulerCleanup(
            HttpServletRequest httpRequest) {
        SearchScheduledJobRecordDto record = searchApplicationService.triggerSchedulerCleanup();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.ok(record, "Scheduler cleanup triggered", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @GetMapping("/admin/scheduler/history")
    @PreAuthorize("hasAuthority('SRH_ADMIN')")
    @Operation(summary = "Get scheduler execution history", description = "Requires SRH_ADMIN permission")
    public ApiResponse<List<SearchScheduledJobRecordDto>> getSchedulerHistory(
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getSchedulerHistory(limit),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Observability (SRH-020) ---

    @GetMapping("/admin/observability")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get observability snapshot", description = "Requires SRH_MONITOR permission")
    public ApiResponse<SearchObservabilitySnapshotDto> getObservabilitySnapshot(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getObservabilitySnapshot(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/traces")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get recent distributed traces", description = "Requires SRH_MONITOR permission")
    public ApiResponse<List<SearchTraceRecord>> getObservabilityTraces(
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getObservabilityTraces(limit),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/metrics")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get observability metrics snapshot", description = "Requires SRH_MONITOR permission")
    public ApiResponse<SearchMetricsSnapshotDto> getObservabilityMetrics(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getObservabilityMetrics(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/latency")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get latency snapshot", description = "Requires SRH_MONITOR permission")
    public ApiResponse<SearchLatencySnapshotDto> getObservabilityLatency(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getObservabilityLatency(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/admin/errors")
    @PreAuthorize("hasAuthority('SRH_MONITOR')")
    @Operation(summary = "Get error snapshot", description = "Requires SRH_MONITOR permission")
    public ApiResponse<SearchErrorSnapshotDto> getObservabilityErrors(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                searchApplicationService.getObservabilityErrors(),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }
}
