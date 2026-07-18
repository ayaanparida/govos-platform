package com.govos.srh.admin;

import com.govos.srh.ai.EmbeddingHealthStatus;

public record SearchSemanticInfoDto(
        String provider,
        int embeddingDimension,
        boolean semanticEnabled,
        String vectorIndexHealth,
        String embeddingProviderHealth,
        long indexedEmbeddingCount,
        String modelName,
        int embeddingVersion,
        String embeddingCacheHealth,
        long embeddingCacheEntries
) {

    public static SearchSemanticInfoDto from(
            String provider,
            int embeddingDimension,
            boolean semanticEnabled,
            EmbeddingHealthStatus vectorIndexHealth,
            EmbeddingHealthStatus embeddingProviderHealth,
            long indexedEmbeddingCount,
            String modelName,
            int embeddingVersion,
            String embeddingCacheHealth,
            long embeddingCacheEntries) {
        return new SearchSemanticInfoDto(
                provider,
                embeddingDimension,
                semanticEnabled,
                vectorIndexHealth != null ? vectorIndexHealth.name() : "UNKNOWN",
                embeddingProviderHealth != null ? embeddingProviderHealth.name() : "UNKNOWN",
                indexedEmbeddingCount,
                modelName,
                embeddingVersion,
                embeddingCacheHealth,
                embeddingCacheEntries);
    }
}
