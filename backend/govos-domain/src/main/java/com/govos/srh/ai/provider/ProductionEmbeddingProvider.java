package com.govos.srh.ai.provider;

import com.govos.srh.ai.EmbeddingHealthStatus;
import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.production.SearchMetricsRecorder;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ProductionEmbeddingProvider implements EmbeddingProvider {

    private final EmbeddingProvider delegate;

    public ProductionEmbeddingProvider(
            EmbeddingProviderFactory factory,
            EmbeddingCache embeddingCache,
            SearchProperties searchProperties,
            SearchMetricsRecorder metricsRecorder) {
        EmbeddingProvider resolved = factory.resolveProvider();
        this.delegate = new CachedEmbeddingProvider(
                resolved,
                embeddingCache,
                metricsRecorder,
                resolveModelName(resolved, searchProperties));
    }

    @PostConstruct
    void validateOnStartup() {
        // Factory validation is invoked from SemanticAiConfiguration
    }

    @Override
    public float[] generateEmbedding(String text) {
        return delegate.generateEmbedding(text);
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        return delegate.generateEmbeddings(texts);
    }

    @Override
    public int embeddingDimension() {
        return delegate.embeddingDimension();
    }

    @Override
    public EmbeddingHealthStatus health() {
        return delegate.health();
    }

    @Override
    public String providerName() {
        return delegate.providerName();
    }

    private static String resolveModelName(EmbeddingProvider provider, SearchProperties searchProperties) {
        return switch (provider.providerName()) {
            case "openai" -> searchProperties.getSemantic().getOpenai().getModel();
            case "azure-openai" -> searchProperties.getSemantic().getAzure().getDeployment();
            case "ollama" -> searchProperties.getSemantic().getOllama().getModel();
            default -> provider.providerName();
        };
    }
}
