package com.govos.srh.ai;

public record SemanticSearchInfo(
        String provider,
        int embeddingDimension,
        boolean semanticEnabled,
        EmbeddingHealthStatus vectorIndexHealth,
        EmbeddingHealthStatus embeddingProviderHealth,
        long indexedEmbeddingCount,
        String modelName,
        int embeddingVersion,
        String embeddingCacheHealth,
        long embeddingCacheEntries
) {
}
