package com.govos.api.srh.application;

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
import com.govos.srh.query.AutocompleteRequest;
import com.govos.srh.query.AutocompleteResponse;
import com.govos.srh.query.FacetSearchRequest;
import com.govos.srh.query.GeoSearchRequest;
import com.govos.srh.admin.SearchClusterInfoDto;
import com.govos.srh.admin.SearchDashboardDto;
import com.govos.srh.admin.SearchHealthDto;
import com.govos.srh.admin.SearchIndexStatisticsDto;
import com.govos.srh.admin.SearchNodeInfoDto;
import com.govos.srh.admin.SearchQueryStatisticsDto;
import com.govos.srh.admin.SearchSlowQueryDto;
import com.govos.srh.admin.SearchStatisticsDto;
import com.govos.srh.admin.SearchTopQueryDto;
import com.govos.srh.admin.SemanticProviderInfoDto;
import com.govos.srh.scheduler.SearchScheduledJobRecordDto;
import com.govos.srh.scheduler.SearchSchedulerStatusDto;
import com.govos.srh.observability.SearchErrorSnapshotDto;
import com.govos.srh.observability.SearchLatencySnapshotDto;
import com.govos.srh.observability.SearchMetricsSnapshotDto;
import com.govos.srh.observability.SearchObservabilitySnapshotDto;
import com.govos.srh.observability.SearchObservationEvent;
import com.govos.srh.observability.SearchTraceRecord;
import com.govos.srh.ai.HybridSearchRequest;
import com.govos.srh.ai.SemanticSearchRequest;
import com.govos.srh.production.SearchOperationalHealthDto;
import com.govos.srh.query.SearchRequest;
import com.govos.srh.query.SearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SearchApplicationService {

    SearchIndexDto createIndex(SearchIndexCreateRequest request);

    SearchIndexDto updateIndex(UUID id, SearchIndexUpdateRequest request);

    SearchIndexDto getIndex(UUID id);

    SearchIndexDto getIndexByCode(String code);

    Page<SearchIndexDto> listIndexes(Pageable pageable);

    SearchIndexDto activateIndex(UUID id);

    SearchIndexDto archiveIndex(UUID id);

    void softDeleteIndex(UUID id);

    SearchIndexDto restoreIndex(UUID id);

    SearchDocumentDto createDocument(SearchDocumentCreateRequest request);

    SearchDocumentDto updateDocument(UUID id, SearchDocumentUpdateRequest request);

    SearchDocumentDto getDocument(UUID id);

    List<SearchDocumentDto> listDocumentsByIndex(UUID searchIndexId);

    List<SearchDocumentDto> listDocumentsByOrganization(UUID organizationId);

    void softDeleteDocument(UUID id);

    SearchDocumentDto restoreDocument(UUID id);

    SearchAliasDto createAlias(SearchAliasCreateRequest request);

    SearchAliasDto updateAlias(UUID id, SearchAliasUpdateRequest request);

    SearchAliasDto activateAlias(UUID id);

    SearchAliasDto getAlias(String aliasName);

    List<SearchAliasDto> listAliases(UUID searchIndexId);

    void softDeleteAlias(UUID id);

    SearchAliasDto restoreAlias(UUID id);

    SearchSyncJobDto createJob(SearchSyncJobCreateRequest request);

    SearchSyncJobDto updateJob(UUID id, SearchSyncJobUpdateRequest request);

    SearchSyncJobDto startJob(UUID id);

    SearchSyncJobDto completeJob(UUID id);

    SearchSyncJobDto failJob(UUID id);

    SearchSyncJobDto cancelJob(UUID id);

    SearchSyncJobDto getJob(UUID id);

    List<SearchSyncJobDto> listJobs(UUID searchIndexId);

    SearchQueryHistoryDto recordQuery(SearchQueryHistoryDto request);

    List<SearchQueryHistoryDto> listQueriesByOrganization(UUID organizationId);

    List<SearchQueryHistoryDto> listQueriesByUser(UUID userId);

    SearchResponse search(SearchRequest request);

    AutocompleteResponse autocomplete(AutocompleteRequest request);

    SearchResponse facetSearch(FacetSearchRequest request);

    SearchResponse geoSearch(GeoSearchRequest request);

    long count(SearchRequest request);

    List<String> suggest(AutocompleteRequest request);

    // --- Semantic Search (SRH-016) ---

    SearchResponse semanticSearch(SemanticSearchRequest request);

    SearchResponse hybridSearch(HybridSearchRequest request);

    SearchOperationalHealthDto getOperationalHealth();

    SemanticProviderInfoDto getSemanticProviderInfo();

    SearchSchedulerStatusDto getSchedulerStatus();

    SearchScheduledJobRecordDto triggerSchedulerReindex(boolean full);

    SearchScheduledJobRecordDto triggerSchedulerEmbedding();

    SearchScheduledJobRecordDto triggerSchedulerCache();

    SearchScheduledJobRecordDto triggerSchedulerStatistics();

    SearchScheduledJobRecordDto triggerSchedulerCleanup();

    List<SearchScheduledJobRecordDto> getSchedulerHistory(int limit);

    // --- Administration (SRH-015) ---

    SearchHealthDto getClusterHealth();

    SearchClusterInfoDto getClusterInformation();

    List<SearchNodeInfoDto> getNodeInformation();

    SearchStatisticsDto getSearchStatistics();

    SearchIndexStatisticsDto getIndexStatistics(UUID indexId);

    SearchQueryStatisticsDto getQueryStatistics();

    List<SearchTopQueryDto> getTopQueries(int limit);

    List<SearchSlowQueryDto> getSlowQueries(int limit);

    SearchDashboardDto getSearchDashboard();

    SearchSyncJobDto reindexIndex(UUID indexId);

    List<SearchSyncJobDto> reindexAll();

    SearchSyncJobDto cancelReindex(UUID jobId);

    List<SearchSyncJobDto> getRunningJobs();

    // --- Observability (SRH-020) ---

    SearchObservabilitySnapshotDto getObservabilitySnapshot();

    List<SearchTraceRecord> getObservabilityTraces(int limit);

    SearchMetricsSnapshotDto getObservabilityMetrics();

    SearchLatencySnapshotDto getObservabilityLatency();

    SearchErrorSnapshotDto getObservabilityErrors();

    List<SearchObservationEvent> getObservabilityEvents(int limit);
}
