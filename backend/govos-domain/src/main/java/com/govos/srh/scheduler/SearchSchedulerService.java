package com.govos.srh.scheduler;

import java.util.List;

public interface SearchSchedulerService {

    SearchSchedulerStatusDto getStatus();

    SearchScheduledJobRecordDto triggerReindex(boolean full);

    SearchScheduledJobRecordDto triggerEmbeddingGeneration();

    SearchScheduledJobRecordDto triggerCacheMaintenance();

    SearchScheduledJobRecordDto triggerStatisticsRefresh();

    SearchScheduledJobRecordDto triggerCleanup();

    List<SearchScheduledJobRecordDto> getHistory(int limit);

    void runDailyFullReindex();

    void runIncrementalReindex();

    void runEmbeddingGeneration();

    void runEmbeddingRetry();

    void runVectorCleanup();

    void runCacheWarmup();

    void runCacheEviction();

    void runClusterHealthVerification();

    void runQueryHistoryRetention();

    void runSlowQueryAnalysis();

    void runIndexOptimization();

    void runStatisticsAggregation();
}
