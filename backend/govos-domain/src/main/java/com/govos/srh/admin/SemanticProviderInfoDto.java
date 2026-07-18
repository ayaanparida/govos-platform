package com.govos.srh.admin;

public record SemanticProviderInfoDto(
        String configuredProvider,
        String activeProvider,
        String modelName,
        int embeddingDimension,
        int embeddingVersion,
        String providerHealth,
        String vectorStore,
        String embeddingCacheHealth,
        long embeddingCacheEntries
) {
}
