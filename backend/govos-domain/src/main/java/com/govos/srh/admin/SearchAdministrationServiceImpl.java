package com.govos.srh.admin;

import com.govos.srh.ai.SemanticSearchInfo;
import com.govos.srh.ai.SemanticSearchService;
import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.entity.SearchDocument;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.entity.SearchSyncJob;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.mapper.SearchSyncJobMapper;
import com.govos.srh.repository.SearchAliasRepository;
import com.govos.srh.repository.SearchDocumentRepository;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.repository.SearchQueryHistoryRepository;
import com.govos.srh.repository.SearchSyncJobRepository;
import com.govos.srh.service.SearchIndexService;
import com.govos.srh.service.SearchSyncJobService;
import com.govos.srh.scheduler.SearchScheduledJobRecordDto;
import com.govos.srh.scheduler.SearchSchedulerService;
import com.govos.srh.scheduler.SearchSchedulerStatusDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchAdministrationServiceImpl implements SearchAdministrationService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int ANALYTICS_WINDOW_DAYS = 30;

    private final SearchIndexService searchIndexService;
    private final SearchClusterMonitor searchClusterMonitor;
    private final SearchIndexMonitor searchIndexMonitor;
    private final SearchQueryHistoryRepository searchQueryHistoryRepository;
    private final SearchSyncJobRepository searchSyncJobRepository;
    private final SearchSyncJobService searchSyncJobService;
    private final SearchSyncJobMapper searchSyncJobMapper;
    private final SearchIndexRepository searchIndexRepository;
    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchAliasRepository searchAliasRepository;
    private final SearchQueryAnalytics searchQueryAnalytics;
    private final SemanticSearchService semanticSearchService;
    private final SearchSchedulerService searchSchedulerService;

    public SearchAdministrationServiceImpl(
            SearchIndexService searchIndexService,
            SearchClusterMonitor searchClusterMonitor,
            SearchIndexMonitor searchIndexMonitor,
            SearchQueryHistoryRepository searchQueryHistoryRepository,
            SearchSyncJobRepository searchSyncJobRepository,
            SearchSyncJobService searchSyncJobService,
            SearchSyncJobMapper searchSyncJobMapper,
            SearchIndexRepository searchIndexRepository,
            SearchDocumentRepository searchDocumentRepository,
            SearchAliasRepository searchAliasRepository,
            SearchQueryAnalytics searchQueryAnalytics,
            SemanticSearchService semanticSearchService,
            SearchSchedulerService searchSchedulerService) {
        this.searchIndexService = searchIndexService;
        this.searchClusterMonitor = searchClusterMonitor;
        this.searchIndexMonitor = searchIndexMonitor;
        this.searchQueryHistoryRepository = searchQueryHistoryRepository;
        this.searchSyncJobRepository = searchSyncJobRepository;
        this.searchSyncJobService = searchSyncJobService;
        this.searchSyncJobMapper = searchSyncJobMapper;
        this.searchIndexRepository = searchIndexRepository;
        this.searchDocumentRepository = searchDocumentRepository;
        this.searchAliasRepository = searchAliasRepository;
        this.searchQueryAnalytics = searchQueryAnalytics;
        this.semanticSearchService = semanticSearchService;
        this.searchSchedulerService = searchSchedulerService;
    }

    @Override
    public SearchHealthDto getClusterHealth() {
        String engineStatus = searchIndexService.health().name();
        return searchClusterMonitor.getDetailedHealth(engineStatus);
    }

    @Override
    public SearchClusterInfoDto getClusterInformation() {
        return searchClusterMonitor.getClusterInformation();
    }

    @Override
    public List<SearchNodeInfoDto> getNodeInformation() {
        return searchClusterMonitor.getNodeInformation();
    }

    @Override
    public SearchStatisticsDto getSearchStatistics() {
        Instant now = Instant.now();
        List<SearchIndex> indexes = searchIndexRepository.findAll().stream()
                .filter(index -> !Boolean.TRUE.equals(index.getDeleted()))
                .toList();
        long activeIndexes = indexes.stream()
                .filter(index -> Boolean.TRUE.equals(index.getActive()))
                .count();
        long totalDocuments = indexes.stream()
                .mapToLong(index -> index.getActiveDocumentCount() != null ? index.getActiveDocumentCount() : 0L)
                .sum();

        List<SearchQueryHistory> histories = loadRecentQueryHistory(now);
        SearchQueryStatisticsDto queryStats = searchQueryAnalytics.buildQueryStatistics(histories);

        long runningJobs = searchSyncJobRepository
                .findAllByJobStatusAndDeletedFalse(SearchJobStatus.RUNNING)
                .size();
        Instant last24Hours = now.minus(24, ChronoUnit.HOURS);
        long failedJobsLast24Hours = searchSyncJobRepository.findAll().stream()
                .filter(job -> !Boolean.TRUE.equals(job.getDeleted()))
                .filter(job -> job.getStatus() == SearchJobStatus.FAILED)
                .filter(job -> job.getCompletedAt() != null && !job.getCompletedAt().isBefore(last24Hours))
                .count();

        return new SearchStatisticsDto(
                indexes.size(),
                activeIndexes,
                totalDocuments,
                queryStats.totalQueries(),
                queryStats.averageResponseTimeMs(),
                runningJobs,
                failedJobsLast24Hours,
                now);
    }

    @Override
    public SearchIndexStatisticsDto getIndexStatistics(UUID indexId) {
        SearchIndex index = searchIndexRepository.findByIdAndDeletedFalse(indexId)
                .orElseThrow(() -> new SearchIndexNotFoundException("Search index not found with id: " + indexId));

        List<SearchDocument> documents = searchDocumentRepository.findAllBySearchIndexIdAndDeletedFalse(indexId);

        Instant lastIndexedAt = documents.stream()
                .map(SearchDocument::getLastIndexedAt)
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        String activeAlias = searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(indexId).stream()
                .filter(alias -> Boolean.TRUE.equals(alias.getActiveAlias()))
                .map(SearchAlias::getAliasName)
                .findFirst()
                .orElse(index.getAliasName());

        String physicalIndexName = index.getPhysicalIndexName();
        SearchIndexEngineStats engineStats = searchIndexMonitor.getIndexStats(physicalIndexName);

        long searchCount = countIndexSearches(index, loadRecentQueryHistory(Instant.now()));

        return new SearchIndexStatisticsDto(
                index.getId(),
                index.getCode(),
                engineStats.documentCount() > 0 ? engineStats.documentCount() : documents.size(),
                engineStats.deletedCount(),
                engineStats.storageSizeBytes(),
                lastIndexedAt,
                index.getLastReindexedAt(),
                activeAlias,
                index.getMappingVersion(),
                searchCount);
    }

    @Override
    public SearchQueryStatisticsDto getQueryStatistics() {
        return searchQueryAnalytics.buildQueryStatistics(loadRecentQueryHistory(Instant.now()));
    }

    @Override
    public List<SearchTopQueryDto> getTopQueries(int limit) {
        return searchQueryAnalytics.topQueries(loadRecentQueryHistory(Instant.now()), resolveLimit(limit));
    }

    @Override
    public List<SearchTopQueryDto> getTopOrganizations(int limit) {
        return searchQueryAnalytics.topOrganizations(loadRecentQueryHistory(Instant.now()), resolveLimit(limit));
    }

    @Override
    public List<SearchTopQueryDto> getTopEntityTypes(int limit) {
        return searchQueryAnalytics.topEntityTypes(loadRecentQueryHistory(Instant.now()), resolveLimit(limit));
    }

    @Override
    public List<SearchTopQueryDto> getTopFilters(int limit) {
        return searchQueryAnalytics.topFilters(loadRecentQueryHistory(Instant.now()), resolveLimit(limit));
    }

    @Override
    public List<SearchSlowQueryDto> getSlowQueries(int limit) {
        return searchQueryAnalytics.slowQueries(loadRecentQueryHistory(Instant.now()), resolveLimit(limit));
    }

    @Override
    @Transactional
    public SearchSyncJobDto reindexIndex(UUID indexId) {
        SearchIndex index = searchIndexRepository.findByIdAndDeletedFalse(indexId)
                .orElseThrow(() -> new SearchIndexNotFoundException("Search index not found with id: " + indexId));

        SearchAlias activeAlias = resolveActiveAlias(indexId);
        SearchSyncJobDto job = searchSyncJobService.create(new SearchSyncJobCreateRequest(
                null,
                indexId,
                "Reindex " + index.getCode(),
                SearchJobType.FULL_REINDEX,
                true));

        searchSyncJobService.start(job.id());
        try {
            searchIndexService.switchAlias(indexId, activeAlias.getId());
            return searchSyncJobService.complete(job.id());
        } catch (RuntimeException ex) {
            searchSyncJobService.fail(job.id());
            throw new SearchAdministrationException("Reindex failed for index: " + indexId, ex);
        }
    }

    @Override
    @Transactional
    public List<SearchSyncJobDto> reindexAll() {
        List<SearchSyncJobDto> jobs = new ArrayList<>();
        for (SearchIndex index : searchIndexRepository.findAllByActiveTrueAndDeletedFalse()) {
            jobs.add(reindexIndex(index.getId()));
        }
        return jobs;
    }

    @Override
    @Transactional
    public SearchSyncJobDto cancelReindex(UUID jobId) {
        return searchSyncJobService.cancel(jobId);
    }

    @Override
    public List<SearchSyncJobDto> getRunningJobs() {
        return searchSyncJobRepository.findAllByJobStatusAndDeletedFalse(SearchJobStatus.RUNNING).stream()
                .map(searchSyncJobMapper::toDto)
                .toList();
    }

    @Override
    public SearchDashboardDto getSearchDashboard() {
        Instant now = Instant.now();
        SearchHealthDto health = getClusterHealth();
        SearchQueryStatisticsDto queryStatistics = getQueryStatistics();
        SearchStatisticsDto platformStatistics = getSearchStatistics();

        List<SearchSyncJobDto> runningJobs = getRunningJobs();
        List<SearchSyncJobDto> recentFailures = searchSyncJobRepository.findAll().stream()
                .filter(job -> !Boolean.TRUE.equals(job.getDeleted()))
                .filter(job -> job.getStatus() == SearchJobStatus.FAILED)
                .sorted(Comparator.comparing(
                        SearchSyncJob::getCompletedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(searchSyncJobMapper::toDto)
                .toList();

        List<SearchIndexUsageDto> indexUsage = searchIndexRepository.findAllByActiveTrueAndDeletedFalse().stream()
                .map(index -> new SearchIndexUsageDto(
                        index.getId(),
                        index.getCode(),
                        index.getActiveDocumentCount() != null ? index.getActiveDocumentCount() : 0L,
                        index.getLastReindexedAt()))
                .toList();

        SearchSemanticInfoDto semanticInfo = toSemanticInfo(semanticSearchService.getSemanticInfo());

        return new SearchDashboardDto(
                health,
                queryStatistics,
                platformStatistics,
                runningJobs,
                recentFailures,
                indexUsage,
                semanticInfo,
                now);
    }

    private static SearchSemanticInfoDto toSemanticInfo(SemanticSearchInfo info) {
        return SearchSemanticInfoDto.from(
                info.provider(),
                info.embeddingDimension(),
                info.semanticEnabled(),
                info.vectorIndexHealth(),
                info.embeddingProviderHealth(),
                info.indexedEmbeddingCount(),
                info.modelName(),
                info.embeddingVersion(),
                info.embeddingCacheHealth(),
                info.embeddingCacheEntries());
    }

    private List<SearchQueryHistory> loadRecentQueryHistory(Instant now) {
        Instant from = now.minus(ANALYTICS_WINDOW_DAYS, ChronoUnit.DAYS);
        return searchQueryHistoryRepository.findAllByCreatedDateBetweenAndDeletedFalse(from, now);
    }

    private SearchAlias resolveActiveAlias(UUID indexId) {
        return searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(indexId).stream()
                .filter(alias -> Boolean.TRUE.equals(alias.getActiveAlias()))
                .findFirst()
                .orElseThrow(() -> new SearchAdministrationException(
                        "No active alias found for search index: " + indexId));
    }

    private long countIndexSearches(SearchIndex index, List<SearchQueryHistory> histories) {
        List<String> entityTypes = searchDocumentRepository.findAllBySearchIndexIdAndDeletedFalse(index.getId()).stream()
                .map(SearchDocument::getEntityType)
                .distinct()
                .toList();

        if (entityTypes.isEmpty()) {
            return 0L;
        }

        return histories.stream()
                .filter(history -> history.getFiltersJson() != null)
                .filter(history -> entityTypes.stream().anyMatch(type -> history.getFiltersJson().contains(type)))
                .count();
    }

    private static int resolveLimit(int limit) {
        return limit > 0 ? limit : DEFAULT_LIMIT;
    }

    @Override
    public SearchSchedulerStatusDto getSchedulerStatus() {
        return searchSchedulerService.getStatus();
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerReindex(boolean full) {
        return searchSchedulerService.triggerReindex(full);
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerEmbedding() {
        return searchSchedulerService.triggerEmbeddingGeneration();
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerCache() {
        return searchSchedulerService.triggerCacheMaintenance();
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerStatistics() {
        return searchSchedulerService.triggerStatisticsRefresh();
    }

    @Override
    public SearchScheduledJobRecordDto triggerSchedulerCleanup() {
        return searchSchedulerService.triggerCleanup();
    }

    @Override
    public List<SearchScheduledJobRecordDto> getSchedulerHistory(int limit) {
        return searchSchedulerService.getHistory(limit);
    }
}
