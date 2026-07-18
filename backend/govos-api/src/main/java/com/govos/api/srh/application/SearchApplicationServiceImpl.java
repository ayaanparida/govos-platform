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
import com.govos.srh.admin.SearchAdministrationService;
import com.govos.srh.admin.SemanticProviderInfoDto;
import com.govos.srh.admin.SearchClusterInfoDto;
import com.govos.srh.admin.SearchDashboardDto;
import com.govos.srh.admin.SearchHealthDto;
import com.govos.srh.admin.SearchIndexStatisticsDto;
import com.govos.srh.admin.SearchNodeInfoDto;
import com.govos.srh.admin.SearchQueryStatisticsDto;
import com.govos.srh.admin.SearchSlowQueryDto;
import com.govos.srh.admin.SearchStatisticsDto;
import com.govos.srh.admin.SearchTopQueryDto;
import com.govos.srh.scheduler.SearchScheduledJobRecordDto;
import com.govos.srh.scheduler.SearchSchedulerStatusDto;
import com.govos.srh.observability.SearchErrorSnapshotDto;
import com.govos.srh.observability.SearchLatencySnapshotDto;
import com.govos.srh.observability.SearchMetricsSnapshotDto;
import com.govos.srh.observability.SearchMonitoringService;
import com.govos.srh.observability.SearchObservabilitySnapshotDto;
import com.govos.srh.observability.SearchObservationEvent;
import com.govos.srh.observability.SearchObservationService;
import com.govos.srh.observability.SearchTraceRecord;
import com.govos.srh.production.SearchOperationalHealthDto;
import com.govos.srh.production.SearchOperationalHealthService;
import com.govos.srh.ai.HybridSearchRequest;
import com.govos.srh.ai.SemanticSearchRequest;
import com.govos.srh.ai.SemanticProviderInfoService;
import com.govos.srh.ai.SemanticSearchService;
import com.govos.srh.service.SearchAliasService;
import com.govos.srh.service.SearchDocumentService;
import com.govos.srh.service.SearchIndexService;
import com.govos.srh.service.SearchQueryHistoryService;
import com.govos.srh.service.SearchSyncJobService;
import com.govos.srh.query.AutocompleteRequest;
import com.govos.srh.query.AutocompleteResponse;
import com.govos.srh.query.FacetSearchRequest;
import com.govos.srh.query.GeoSearchRequest;
import com.govos.srh.query.SearchQueryService;
import com.govos.srh.query.SearchRequest;
import com.govos.srh.query.SearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchApplicationServiceImpl implements SearchApplicationService {

    private final SearchIndexService searchIndexService;
    private final SearchDocumentService searchDocumentService;
    private final SearchAliasService searchAliasService;
    private final SearchSyncJobService searchSyncJobService;
    private final SearchQueryHistoryService searchQueryHistoryService;
    private final SearchQueryService searchQueryService;
    private final SearchAdministrationService searchAdministrationService;
    private final SemanticSearchService semanticSearchService;
    private final SearchOperationalHealthService searchOperationalHealthService;
    private final SemanticProviderInfoService semanticProviderInfoService;
    private final SearchObservationService searchObservationService;
    private final SearchMonitoringService searchMonitoringService;

    public SearchApplicationServiceImpl(
            SearchIndexService searchIndexService,
            SearchDocumentService searchDocumentService,
            SearchAliasService searchAliasService,
            SearchSyncJobService searchSyncJobService,
            SearchQueryHistoryService searchQueryHistoryService,
            SearchQueryService searchQueryService,
            SearchAdministrationService searchAdministrationService,
            SemanticSearchService semanticSearchService,
            SearchOperationalHealthService searchOperationalHealthService,
            SemanticProviderInfoService semanticProviderInfoService,
            SearchObservationService searchObservationService,
            SearchMonitoringService searchMonitoringService) {
        this.searchIndexService = searchIndexService;
        this.searchDocumentService = searchDocumentService;
        this.searchAliasService = searchAliasService;
        this.searchSyncJobService = searchSyncJobService;
        this.searchQueryHistoryService = searchQueryHistoryService;
        this.searchQueryService = searchQueryService;
        this.searchAdministrationService = searchAdministrationService;
        this.semanticSearchService = semanticSearchService;
        this.searchOperationalHealthService = searchOperationalHealthService;
        this.semanticProviderInfoService = semanticProviderInfoService;
        this.searchObservationService = searchObservationService;
        this.searchMonitoringService = searchMonitoringService;
    }

    @Override
    @Transactional
    public SearchIndexDto createIndex(SearchIndexCreateRequest request) {
        return searchIndexService.create(request);
    }

    @Override
    @Transactional
    public SearchIndexDto updateIndex(UUID id, SearchIndexUpdateRequest request) {
        return searchIndexService.update(id, request);
    }

    @Override
    public SearchIndexDto getIndex(UUID id) {
        return searchIndexService.getById(id);
    }

    @Override
    public SearchIndexDto getIndexByCode(String code) {
        return searchIndexService.getByCode(code);
    }

    @Override
    public Page<SearchIndexDto> listIndexes(Pageable pageable) {
        return searchIndexService.list(pageable);
    }

    @Override
    @Transactional
    public SearchIndexDto activateIndex(UUID id) {
        return searchIndexService.activate(id);
    }

    @Override
    @Transactional
    public SearchIndexDto archiveIndex(UUID id) {
        return searchIndexService.archive(id);
    }

    @Override
    @Transactional
    public void softDeleteIndex(UUID id) {
        searchIndexService.softDelete(id);
    }

    @Override
    @Transactional
    public SearchIndexDto restoreIndex(UUID id) {
        return searchIndexService.restore(id);
    }

    @Override
    @Transactional
    public SearchDocumentDto createDocument(SearchDocumentCreateRequest request) {
        return searchDocumentService.create(request);
    }

    @Override
    @Transactional
    public SearchDocumentDto updateDocument(UUID id, SearchDocumentUpdateRequest request) {
        return searchDocumentService.update(id, request);
    }

    @Override
    public SearchDocumentDto getDocument(UUID id) {
        return searchDocumentService.getById(id);
    }

    @Override
    public List<SearchDocumentDto> listDocumentsByIndex(UUID searchIndexId) {
        return searchDocumentService.listByIndex(searchIndexId);
    }

    @Override
    public List<SearchDocumentDto> listDocumentsByOrganization(UUID organizationId) {
        return searchDocumentService.listByOrganization(organizationId);
    }

    @Override
    @Transactional
    public void softDeleteDocument(UUID id) {
        searchDocumentService.softDelete(id);
    }

    @Override
    @Transactional
    public SearchDocumentDto restoreDocument(UUID id) {
        return searchDocumentService.restore(id);
    }

    @Override
    @Transactional
    public SearchAliasDto createAlias(SearchAliasCreateRequest request) {
        return searchAliasService.create(request);
    }

    @Override
    @Transactional
    public SearchAliasDto updateAlias(UUID id, SearchAliasUpdateRequest request) {
        return searchAliasService.update(id, request);
    }

    @Override
    @Transactional
    public SearchAliasDto activateAlias(UUID id) {
        return searchAliasService.activateAlias(id);
    }

    @Override
    public SearchAliasDto getAlias(String aliasName) {
        return searchAliasService.getByAlias(aliasName);
    }

    @Override
    public List<SearchAliasDto> listAliases(UUID searchIndexId) {
        return searchAliasService.listByIndex(searchIndexId);
    }

    @Override
    @Transactional
    public void softDeleteAlias(UUID id) {
        searchAliasService.softDelete(id);
    }

    @Override
    @Transactional
    public SearchAliasDto restoreAlias(UUID id) {
        return searchAliasService.restore(id);
    }

    @Override
    @Transactional
    public SearchSyncJobDto createJob(SearchSyncJobCreateRequest request) {
        return searchSyncJobService.create(request);
    }

    @Override
    @Transactional
    public SearchSyncJobDto updateJob(UUID id, SearchSyncJobUpdateRequest request) {
        return searchSyncJobService.update(id, request);
    }

    @Override
    @Transactional
    public SearchSyncJobDto startJob(UUID id) {
        return searchSyncJobService.start(id);
    }

    @Override
    @Transactional
    public SearchSyncJobDto completeJob(UUID id) {
        return searchSyncJobService.complete(id);
    }

    @Override
    @Transactional
    public SearchSyncJobDto failJob(UUID id) {
        return searchSyncJobService.fail(id);
    }

    @Override
    @Transactional
    public SearchSyncJobDto cancelJob(UUID id) {
        return searchSyncJobService.cancel(id);
    }

    @Override
    public SearchSyncJobDto getJob(UUID id) {
        return searchSyncJobService.getById(id);
    }

    @Override
    public List<SearchSyncJobDto> listJobs(UUID searchIndexId) {
        return searchSyncJobService.listByIndex(searchIndexId);
    }

    @Override
    @Transactional
    public SearchQueryHistoryDto recordQuery(SearchQueryHistoryDto request) {
        return searchQueryHistoryService.record(request);
    }

    @Override
    public List<SearchQueryHistoryDto> listQueriesByOrganization(UUID organizationId) {
        return searchQueryHistoryService.listByOrganization(organizationId);
    }

    @Override
    public List<SearchQueryHistoryDto> listQueriesByUser(UUID userId) {
        return searchQueryHistoryService.listByUser(userId);
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        return searchQueryService.search(request);
    }

    @Override
    public AutocompleteResponse autocomplete(AutocompleteRequest request) {
        return searchQueryService.autocomplete(request);
    }

    @Override
    public SearchResponse facetSearch(FacetSearchRequest request) {
        return searchQueryService.facetSearch(request);
    }

    @Override
    public SearchResponse geoSearch(GeoSearchRequest request) {
        return searchQueryService.geoSearch(request);
    }

    @Override
    public long count(SearchRequest request) {
        return searchQueryService.count(request);
    }

    @Override
    public List<String> suggest(AutocompleteRequest request) {
        return searchQueryService.suggest(request);
    }

    @Override
    public SearchResponse semanticSearch(SemanticSearchRequest request) {
        return semanticSearchService.semanticSearch(request);
    }

    @Override
    public SearchResponse hybridSearch(HybridSearchRequest request) {
        return semanticSearchService.hybridSearch(request);
    }

    @Override
    public SearchOperationalHealthDto getOperationalHealth() {
        return searchOperationalHealthService.getOperationalHealth();
    }

    @Override
    public SemanticProviderInfoDto getSemanticProviderInfo() {
        return semanticProviderInfoService.getProviderInfo();
    }

    @Override
    public SearchSchedulerStatusDto getSchedulerStatus() {
        return searchAdministrationService.getSchedulerStatus();
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerReindex(boolean full) {
        return searchAdministrationService.triggerSchedulerReindex(full);
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerEmbedding() {
        return searchAdministrationService.triggerSchedulerEmbedding();
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerCache() {
        return searchAdministrationService.triggerSchedulerCache();
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerStatistics() {
        return searchAdministrationService.triggerSchedulerStatistics();
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerCleanup() {
        return searchAdministrationService.triggerSchedulerCleanup();
    }

    @Override
    public List<SearchScheduledJobRecordDto> getSchedulerHistory(int limit) {
        return searchAdministrationService.getSchedulerHistory(limit);
    }

    @Override
    public SearchHealthDto getClusterHealth() {
        return searchAdministrationService.getClusterHealth();
    }

    @Override
    public SearchClusterInfoDto getClusterInformation() {
        return searchAdministrationService.getClusterInformation();
    }

    @Override
    public List<SearchNodeInfoDto> getNodeInformation() {
        return searchAdministrationService.getNodeInformation();
    }

    @Override
    public SearchStatisticsDto getSearchStatistics() {
        return searchAdministrationService.getSearchStatistics();
    }

    @Override
    public SearchIndexStatisticsDto getIndexStatistics(UUID indexId) {
        return searchAdministrationService.getIndexStatistics(indexId);
    }

    @Override
    public SearchQueryStatisticsDto getQueryStatistics() {
        return searchAdministrationService.getQueryStatistics();
    }

    @Override
    public List<SearchTopQueryDto> getTopQueries(int limit) {
        return searchAdministrationService.getTopQueries(limit);
    }

    @Override
    public List<SearchSlowQueryDto> getSlowQueries(int limit) {
        return searchAdministrationService.getSlowQueries(limit);
    }

    @Override
    public SearchDashboardDto getSearchDashboard() {
        return searchAdministrationService.getSearchDashboard();
    }

    @Override
    @Transactional
    public SearchSyncJobDto reindexIndex(UUID indexId) {
        return searchAdministrationService.reindexIndex(indexId);
    }

    @Override
    @Transactional
    public List<SearchSyncJobDto> reindexAll() {
        return searchAdministrationService.reindexAll();
    }

    @Override
    @Transactional
    public SearchSyncJobDto cancelReindex(UUID jobId) {
        return searchAdministrationService.cancelReindex(jobId);
    }

    @Override
    public List<SearchSyncJobDto> getRunningJobs() {
        return searchAdministrationService.getRunningJobs();
    }

    @Override
    public SearchObservabilitySnapshotDto getObservabilitySnapshot() {
        return searchObservationService.getSnapshot();
    }

    @Override
    public List<SearchTraceRecord> getObservabilityTraces(int limit) {
        return searchObservationService.getRecentTraces(limit);
    }

    @Override
    public SearchMetricsSnapshotDto getObservabilityMetrics() {
        return searchMonitoringService.getMetricsSnapshot();
    }

    @Override
    public SearchLatencySnapshotDto getObservabilityLatency() {
        return searchMonitoringService.getLatencySnapshot();
    }

    @Override
    public SearchErrorSnapshotDto getObservabilityErrors() {
        return searchMonitoringService.getErrorSnapshot();
    }

    @Override
    public List<SearchObservationEvent> getObservabilityEvents(int limit) {
        return searchObservationService.getRecentEvents(limit);
    }
}
