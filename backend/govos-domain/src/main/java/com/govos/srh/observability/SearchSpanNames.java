package com.govos.srh.observability;

public final class SearchSpanNames {

    public static final String SEARCH_QUERY = "search.query";
    public static final String AUTOCOMPLETE = "search.autocomplete";
    public static final String FACET_SEARCH = "search.facet";
    public static final String GEO_SEARCH = "search.geo";
    public static final String SEMANTIC_SEARCH = "search.semantic";
    public static final String HYBRID_SEARCH = "search.hybrid";
    public static final String EMBEDDING_GENERATION = "search.embedding.generation";
    public static final String VECTOR_INDEXING = "search.vector.index";
    public static final String BULK_INDEXING = "search.bulk.index";
    public static final String ALIAS_SWITCH = "search.alias.switch";
    public static final String REINDEX = "search.reindex";
    public static final String SCHEDULER_EXECUTION = "search.scheduler.execution";
    public static final String CLUSTER_HEALTH = "search.cluster.health";

    private SearchSpanNames() {
    }
}
