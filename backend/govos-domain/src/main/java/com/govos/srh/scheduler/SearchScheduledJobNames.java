package com.govos.srh.scheduler;

public final class SearchScheduledJobNames {

    public static final String DAILY_FULL_REINDEX = "daily-full-reindex";
    public static final String INCREMENTAL_REINDEX = "incremental-reindex";
    public static final String EMBEDDING_GENERATION = "embedding-generation";
    public static final String EMBEDDING_RETRY = "embedding-retry";
    public static final String VECTOR_CLEANUP = "vector-cleanup";
    public static final String CACHE_WARMUP = "cache-warmup";
    public static final String CACHE_EVICTION = "cache-eviction";
    public static final String CLUSTER_HEALTH = "cluster-health-verification";
    public static final String QUERY_HISTORY_RETENTION = "query-history-retention";
    public static final String SLOW_QUERY_ANALYSIS = "slow-query-analysis";
    public static final String INDEX_OPTIMIZATION = "index-optimization";
    public static final String STATISTICS_AGGREGATION = "statistics-aggregation";

    private SearchScheduledJobNames() {
    }
}
