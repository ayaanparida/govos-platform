package com.govos.srh.engine;

public record SearchEngineQuery(
        String indexName,
        String queryText,
        int from,
        int size
) {
}
