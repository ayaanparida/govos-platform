package com.govos.srh.scheduler;

import com.govos.srh.admin.SearchAdministrationService;
import com.govos.srh.admin.SearchClusterMonitor;
import com.govos.srh.admin.SearchIndexMonitor;
import com.govos.srh.ai.SemanticSearchService;
import com.govos.srh.ai.job.EmbeddingDocumentTarget;
import com.govos.srh.ai.job.EmbeddingGenerationService;
import com.govos.srh.ai.provider.EmbeddingCache;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.entity.SearchDocument;
import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.enums.SearchDocumentStatus;
import com.govos.srh.production.SearchOperationalHealthService;
import com.govos.srh.production.SearchReadCache;
import com.govos.srh.repository.SearchDocumentRepository;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.repository.SearchQueryHistoryRepository;
import com.govos.srh.service.SearchIndexService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchSchedulerServiceImpl implements SearchSchedulerService {

    private static final List<String> REGISTERED_JOBS = List.of(
            SearchScheduledJobNames.DAILY_FULL_REINDEX,
            SearchScheduledJobNames.INCREMENTAL_REINDEX,
            SearchScheduledJobNames.EMBEDDING_GENERATION,
            SearchScheduledJobNames.EMBEDDING_RETRY,
            SearchScheduledJobNames.VECTOR_CLEANUP,
            SearchScheduledJobNames.CACHE_WARMUP,
            SearchScheduledJobNames.CACHE_EVICTION,
            SearchScheduledJobNames.CLUSTER_HEALTH,
            SearchScheduledJobNames.QUERY_HISTORY_RETENTION,
            SearchScheduledJobNames.SLOW_QUERY_ANALYSIS,
            SearchScheduledJobNames.INDEX_OPTIMIZATION,
            SearchScheduledJobNames.STATISTICS_AGGREGATION);

    private final SearchProperties searchProperties;
    private final SearchAdministrationService searchAdministrationService;
    private final SearchOperationalHealthService operationalHealthService;
    private final SearchIndexService searchIndexService;
    private final SearchClusterMonitor searchClusterMonitor;
    private final SearchIndexMonitor searchIndexMonitor;
    private final SearchReadCache searchReadCache;
    private final EmbeddingCache embeddingCache;
    private final EmbeddingGenerationService embeddingGenerationService;
    private final SemanticSearchService semanticSearchService;
    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchIndexRepository searchIndexRepository;
    private final SearchQueryHistoryRepository searchQueryHistoryRepository;
    private final SearchSchedulerHistoryStore historyStore;
    private final SearchSchedulerRetryExecutor retryExecutor;
    private final SearchSchedulerMetrics schedulerMetrics;
    private final SearchSchedulerLogger schedulerLogger;

    public SearchSchedulerServiceImpl(
            SearchProperties searchProperties,
            SearchAdministrationService searchAdministrationService,
            SearchOperationalHealthService operationalHealthService,
            SearchIndexService searchIndexService,
            SearchClusterMonitor searchClusterMonitor,
            SearchIndexMonitor searchIndexMonitor,
            SearchReadCache searchReadCache,
            EmbeddingCache embeddingCache,
            EmbeddingGenerationService embeddingGenerationService,
            SemanticSearchService semanticSearchService,
            SearchDocumentRepository searchDocumentRepository,
            SearchIndexRepository searchIndexRepository,
            SearchQueryHistoryRepository searchQueryHistoryRepository,
            SearchSchedulerHistoryStore historyStore,
            SearchSchedulerRetryExecutor retryExecutor,
            SearchSchedulerMetrics schedulerMetrics,
            SearchSchedulerLogger schedulerLogger) {
        this.searchProperties = searchProperties;
        this.searchAdministrationService = searchAdministrationService;
        this.operationalHealthService = operationalHealthService;
        this.searchIndexService = searchIndexService;
        this.searchClusterMonitor = searchClusterMonitor;
        this.searchIndexMonitor = searchIndexMonitor;
        this.searchReadCache = searchReadCache;
        this.embeddingCache = embeddingCache;
        this.embeddingGenerationService = embeddingGenerationService;
        this.semanticSearchService = semanticSearchService;
        this.searchDocumentRepository = searchDocumentRepository;
        this.searchIndexRepository = searchIndexRepository;
        this.searchQueryHistoryRepository = searchQueryHistoryRepository;
        this.historyStore = historyStore;
        this.retryExecutor = retryExecutor;
        this.schedulerMetrics = schedulerMetrics;
        this.schedulerLogger = schedulerLogger;
    }

    @Override
    public SearchSchedulerStatusDto getStatus() {
        SearchSchedulerProperties scheduler = searchProperties.getScheduler();
        return new SearchSchedulerStatusDto(
                scheduler.isEnabled(),
                scheduler.getReindexCron(),
                scheduler.getIncrementalReindexCron(),
                scheduler.getEmbeddingCron(),
                scheduler.getCleanupCron(),
                scheduler.getHealthCron(),
                scheduler.getStatisticsCron(),
                scheduler.getMaxRetries(),
                historyStore.totalExecutions(),
                historyStore.failedExecutions(),
                REGISTERED_JOBS);
    }

    @Override
    public SearchScheduledJobRecordDto triggerReindex(boolean full) {
        return executeJob(
                full ? SearchScheduledJobNames.DAILY_FULL_REINDEX : SearchScheduledJobNames.INCREMENTAL_REINDEX,
                () -> full ? runFullReindexInternal() : runIncrementalReindexInternal());
    }

    @Override
    public SearchScheduledJobRecordDto triggerEmbeddingGeneration() {
        return executeJob(SearchScheduledJobNames.EMBEDDING_GENERATION, this::runEmbeddingGenerationInternal);
    }

    @Override
    public SearchScheduledJobRecordDto triggerCacheMaintenance() {
        return executeJob(SearchScheduledJobNames.CACHE_WARMUP, () -> {
            long warmed = runCacheWarmupInternal();
            runCacheEvictionInternal();
            return warmed;
        });
    }

    @Override
    public SearchScheduledJobRecordDto triggerStatisticsRefresh() {
        return executeJob(SearchScheduledJobNames.STATISTICS_AGGREGATION, this::runStatisticsAggregationInternal);
    }

    @Override
    public SearchScheduledJobRecordDto triggerCleanup() {
        return executeJob(SearchScheduledJobNames.QUERY_HISTORY_RETENTION, () -> {
            long retained = runQueryHistoryRetentionInternal();
            runVectorCleanupInternal();
            return retained;
        });
    }

    @Override
    public List<SearchScheduledJobRecordDto> getHistory(int limit) {
        return historyStore.list(limit).stream()
                .map(SearchScheduledJobRecordDto::from)
                .toList();
    }

    @Override
    public void runDailyFullReindex() {
        if (!isSchedulerEnabled()) {
            return;
        }
        triggerReindex(true);
    }

    @Override
    public void runIncrementalReindex() {
        if (!isSchedulerEnabled()) {
            return;
        }
        triggerReindex(false);
    }

    @Override
    public void runEmbeddingGeneration() {
        if (!isSchedulerEnabled()) {
            return;
        }
        triggerEmbeddingGeneration();
    }

    @Override
    public void runEmbeddingRetry() {
        if (!isSchedulerEnabled()) {
            return;
        }
        executeJob(SearchScheduledJobNames.EMBEDDING_RETRY, this::runEmbeddingRetryInternal);
    }

    @Override
    public void runVectorCleanup() {
        if (!isSchedulerEnabled()) {
            return;
        }
        executeJob(SearchScheduledJobNames.VECTOR_CLEANUP, this::runVectorCleanupInternal);
    }

    @Override
    public void runCacheWarmup() {
        if (!isSchedulerEnabled()) {
            return;
        }
        executeJob(SearchScheduledJobNames.CACHE_WARMUP, this::runCacheWarmupInternal);
    }

    @Override
    public void runCacheEviction() {
        if (!isSchedulerEnabled()) {
            return;
        }
        executeJob(SearchScheduledJobNames.CACHE_EVICTION, this::runCacheEvictionInternal);
    }

    @Override
    public void runClusterHealthVerification() {
        if (!isSchedulerEnabled()) {
            return;
        }
        executeJob(SearchScheduledJobNames.CLUSTER_HEALTH, this::runClusterHealthVerificationInternal);
    }

    @Override
    public void runQueryHistoryRetention() {
        if (!isSchedulerEnabled()) {
            return;
        }
        executeJob(SearchScheduledJobNames.QUERY_HISTORY_RETENTION, this::runQueryHistoryRetentionInternal);
    }

    @Override
    public void runSlowQueryAnalysis() {
        if (!isSchedulerEnabled()) {
            return;
        }
        executeJob(SearchScheduledJobNames.SLOW_QUERY_ANALYSIS, this::runSlowQueryAnalysisInternal);
    }

    @Override
    public void runIndexOptimization() {
        if (!isSchedulerEnabled()) {
            return;
        }
        executeJob(SearchScheduledJobNames.INDEX_OPTIMIZATION, this::runIndexOptimizationInternal);
    }

    @Override
    public void runStatisticsAggregation() {
        if (!isSchedulerEnabled()) {
            return;
        }
        triggerStatisticsRefresh();
    }

    private SearchScheduledJobRecordDto executeJob(String jobName, ScheduledJobAction action) {
        SearchScheduledJobRecord record = new SearchScheduledJobRecord(UUID.randomUUID(), jobName);
        historyStore.add(record);
        schedulerMetrics.recordExecution(jobName);
        long started = System.currentTimeMillis();

        try {
            retryExecutor.execute(jobName, () -> record.setDocumentsProcessed(action.execute()));
            record.setStatus(SearchScheduledJobStatus.COMPLETED);
            record.setErrorMessage(null);
        } catch (RuntimeException ex) {
            record.setStatus(SearchScheduledJobStatus.FAILED);
            record.setErrorMessage(ex.getClass().getSimpleName());
            schedulerMetrics.recordFailure(jobName);
            schedulerLogger.logExecution(
                    jobName,
                    SearchScheduledJobStatus.FAILED.name(),
                    System.currentTimeMillis() - started,
                    record.getDocumentsProcessed(),
                    ex.getClass().getSimpleName());
            throw ex;
        } finally {
            record.setCompletedAt(Instant.now());
            record.setDurationMs(System.currentTimeMillis() - started);
            schedulerMetrics.recordDuration(jobName, record.getDurationMs());
            if (record.getStatus() == SearchScheduledJobStatus.COMPLETED) {
                schedulerLogger.logExecution(
                        jobName,
                        record.getStatus().name(),
                        record.getDurationMs(),
                        record.getDocumentsProcessed(),
                        null);
            }
        }

        return SearchScheduledJobRecordDto.from(record);
    }

    private long runFullReindexInternal() {
        return searchAdministrationService.reindexAll().size();
    }

    private long runIncrementalReindexInternal() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        java.util.Set<UUID> indexIds = new java.util.LinkedHashSet<>();
        for (SearchDocument document : searchDocumentRepository.findAllByLastIndexedAtBeforeAndDeletedFalse(cutoff)) {
            if (document.getSearchIndex() != null) {
                indexIds.add(document.getSearchIndex().getId());
            }
        }
        for (UUID indexId : indexIds) {
            searchAdministrationService.reindexIndex(indexId);
        }
        return indexIds.size();
    }

    private long runEmbeddingGenerationInternal() {
        if (!searchProperties.getSemantic().isEnabled()) {
            schedulerMetrics.recordSkipped(SearchScheduledJobNames.EMBEDDING_GENERATION);
            return 0L;
        }
        List<EmbeddingDocumentTarget> targets = new ArrayList<>();
        for (SearchDocument document : searchDocumentRepository.findAllByDocumentStatusAndDeletedFalse(
                SearchDocumentStatus.NOT_INDEXED)) {
            String text = document.getSearchText();
            if (text == null || text.isBlank()) {
                continue;
            }
            targets.add(new EmbeddingDocumentTarget(
                    document.getReferenceId(),
                    document.getOrganizationId(),
                    document.getEntityType(),
                    text));
        }
        if (targets.isEmpty()) {
            schedulerMetrics.recordSkipped(SearchScheduledJobNames.EMBEDDING_GENERATION);
            return 0L;
        }
        return embeddingGenerationService.startJob(targets).getProcessedDocuments();
    }

    private long runEmbeddingRetryInternal() {
        SearchScheduledJobRecord failed = historyStore.findLatestFailed(SearchScheduledJobNames.EMBEDDING_GENERATION);
        if (failed == null) {
            schedulerMetrics.recordSkipped(SearchScheduledJobNames.EMBEDDING_RETRY);
            return 0L;
        }
        failed.incrementRetryCount();
        return runEmbeddingGenerationInternal();
    }

    private long runVectorCleanupInternal() {
        semanticSearchService.getSemanticInfo();
        return 0L;
    }

    private long runCacheWarmupInternal() {
        if (!searchProperties.getCache().isEnabled()) {
            schedulerMetrics.recordSkipped(SearchScheduledJobNames.CACHE_WARMUP);
            return 0L;
        }
        searchReadCache.put("srh:cluster:info", searchClusterMonitor.getClusterInformation());
        searchReadCache.put("srh:cluster:nodes", searchClusterMonitor.getNodeInformation());
        return 1L;
    }

    private long runCacheEvictionInternal() {
        searchReadCache.evictAll();
        embeddingCache.evictAll();
        return 0L;
    }

    private long runClusterHealthVerificationInternal() {
        operationalHealthService.getOperationalHealth();
        searchIndexService.health();
        return 1L;
    }

    @Transactional
    protected long runQueryHistoryRetentionInternal() {
        Instant cutoff = Instant.now().minus(
                searchProperties.getScheduler().getQueryHistoryRetentionDays(),
                ChronoUnit.DAYS);
        List<SearchQueryHistory> expired = searchQueryHistoryRepository
                .findAllByCreatedDateBetweenAndDeletedFalse(Instant.EPOCH, cutoff);
        for (SearchQueryHistory history : expired) {
            history.setDeleted(true);
            searchQueryHistoryRepository.save(history);
        }
        return expired.size();
    }

    private long runSlowQueryAnalysisInternal() {
        return searchAdministrationService.getSlowQueries(10).size();
    }

    private long runIndexOptimizationInternal() {
        long processed = 0L;
        for (var index : searchIndexRepository.findAllByActiveTrueAndDeletedFalse()) {
            if (index.getPhysicalIndexName() != null) {
                searchIndexMonitor.getIndexStats(index.getPhysicalIndexName());
                processed++;
            }
        }
        return processed;
    }

    private long runStatisticsAggregationInternal() {
        searchAdministrationService.getSearchStatistics();
        searchAdministrationService.getQueryStatistics();
        searchAdministrationService.getSearchDashboard();
        return 1L;
    }

    private boolean isSchedulerEnabled() {
        return searchProperties.getScheduler().isEnabled();
    }
}
