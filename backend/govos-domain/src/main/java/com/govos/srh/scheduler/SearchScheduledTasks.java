package com.govos.srh.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "govos.search.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SearchScheduledTasks {

    private final SearchSchedulerService searchSchedulerService;

    public SearchScheduledTasks(SearchSchedulerService searchSchedulerService) {
        this.searchSchedulerService = searchSchedulerService;
    }

    @Scheduled(cron = "${govos.search.scheduler.reindex-cron:0 0 2 * * *}")
    public void dailyFullReindex() {
        searchSchedulerService.runDailyFullReindex();
    }

    @Scheduled(cron = "${govos.search.scheduler.incremental-reindex-cron:0 0 */6 * * *}")
    public void incrementalReindex() {
        searchSchedulerService.runIncrementalReindex();
    }

    @Scheduled(cron = "${govos.search.scheduler.embedding-cron:0 30 3 * * *}")
    public void embeddingGeneration() {
        searchSchedulerService.runEmbeddingGeneration();
        searchSchedulerService.runEmbeddingRetry();
    }

    @Scheduled(cron = "${govos.search.scheduler.cleanup-cron:0 0 4 * * *}")
    public void cleanupTasks() {
        searchSchedulerService.runQueryHistoryRetention();
        searchSchedulerService.runVectorCleanup();
        searchSchedulerService.runCacheEviction();
    }

    @Scheduled(cron = "${govos.search.scheduler.health-cron:0 */15 * * * *}")
    public void healthVerification() {
        searchSchedulerService.runClusterHealthVerification();
        searchSchedulerService.runSlowQueryAnalysis();
        searchSchedulerService.runIndexOptimization();
    }

    @Scheduled(cron = "${govos.search.scheduler.statistics-cron:0 5 * * * *}")
    public void statisticsAndCache() {
        searchSchedulerService.runStatisticsAggregation();
        searchSchedulerService.runCacheWarmup();
    }
}
