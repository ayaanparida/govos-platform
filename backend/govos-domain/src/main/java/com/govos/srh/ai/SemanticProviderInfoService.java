package com.govos.srh.ai;

import com.govos.srh.admin.SemanticProviderInfoDto;
import com.govos.srh.ai.provider.EmbeddingCache;
import com.govos.srh.ai.provider.EmbeddingProviderFactory;
import com.govos.srh.config.SearchProperties;
import org.springframework.stereotype.Service;

@Service
public class SemanticProviderInfoService {

    private final EmbeddingProvider embeddingProvider;
    private final EmbeddingProviderFactory embeddingProviderFactory;
    private final EmbeddingCache embeddingCache;
    private final SearchProperties searchProperties;

    public SemanticProviderInfoService(
            EmbeddingProvider embeddingProvider,
            EmbeddingProviderFactory embeddingProviderFactory,
            EmbeddingCache embeddingCache,
            SearchProperties searchProperties) {
        this.embeddingProvider = embeddingProvider;
        this.embeddingProviderFactory = embeddingProviderFactory;
        this.embeddingCache = embeddingCache;
        this.searchProperties = searchProperties;
    }

    public SemanticProviderInfoDto getProviderInfo() {
        return new SemanticProviderInfoDto(
                embeddingProviderFactory.configuredProviderName(),
                embeddingProvider.providerName(),
                resolveModelName(),
                embeddingProvider.embeddingDimension(),
                searchProperties.getSemantic().getEmbeddingVersion(),
                embeddingProvider.health().name(),
                searchProperties.getSemantic().getVectorStore(),
                embeddingCache.isEnabled() ? "UP" : "DISABLED",
                embeddingCache.size());
    }

    private String resolveModelName() {
        return switch (embeddingProvider.providerName()) {
            case "openai" -> searchProperties.getSemantic().getOpenai().getModel();
            case "azure-openai" -> searchProperties.getSemantic().getAzure().getDeployment();
            case "ollama" -> searchProperties.getSemantic().getOllama().getModel();
            default -> embeddingProvider.providerName();
        };
    }
}
