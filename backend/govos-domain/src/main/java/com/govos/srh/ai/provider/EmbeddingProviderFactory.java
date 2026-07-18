package com.govos.srh.ai.provider;

import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.ai.MockEmbeddingProvider;
import com.govos.srh.config.SearchProperties;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddingProviderFactory.class);

    private final SearchProperties searchProperties;
    private final ObjectProvider<MockEmbeddingProvider> mockProvider;
    private final ObjectProvider<OpenAIEmbeddingProvider> openAiProvider;
    private final ObjectProvider<AzureOpenAIEmbeddingProvider> azureProvider;
    private final ObjectProvider<OllamaEmbeddingProvider> ollamaProvider;

    public EmbeddingProviderFactory(
            SearchProperties searchProperties,
            ObjectProvider<MockEmbeddingProvider> mockProvider,
            ObjectProvider<OpenAIEmbeddingProvider> openAiProvider,
            ObjectProvider<AzureOpenAIEmbeddingProvider> azureProvider,
            ObjectProvider<OllamaEmbeddingProvider> ollamaProvider) {
        this.searchProperties = searchProperties;
        this.mockProvider = mockProvider;
        this.openAiProvider = openAiProvider;
        this.azureProvider = azureProvider;
        this.ollamaProvider = ollamaProvider;
    }

    public EmbeddingProvider resolveProvider() {
        String configured = normalize(searchProperties.getSemantic().getProvider());
        return switch (configured) {
            case "openai" -> resolveOrFallback(openAiProvider, configured);
            case "azure-openai" -> resolveOrFallback(azureProvider, configured);
            case "ollama" -> resolveOrFallback(ollamaProvider, configured);
            case "mock" -> resolveOrFallback(mockProvider, configured);
            default -> fallback("Unsupported semantic provider: " + configured);
        };
    }

    public void validateConfiguration() {
        String configured = normalize(searchProperties.getSemantic().getProvider());
        switch (configured) {
            case "openai" -> validateOpenAi();
            case "azure-openai" -> validateAzure();
            case "ollama" -> validateOllama();
            case "mock" -> LOGGER.info("Semantic provider configured as mock");
            default -> LOGGER.warn("Unknown semantic provider '{}'; mock fallback will be used", configured);
        }
    }

    public String configuredProviderName() {
        return normalize(searchProperties.getSemantic().getProvider());
    }

    private void validateOpenAi() {
        var openAi = searchProperties.getSemantic().getOpenai();
        if (openAi.getApiKey() == null || openAi.getApiKey().isBlank()) {
            LOGGER.warn("OpenAI provider selected but api-key is missing; mock fallback will be used");
        }
    }

    private void validateAzure() {
        var azure = searchProperties.getSemantic().getAzure();
        if (azure.getApiKey() == null || azure.getApiKey().isBlank()
                || azure.getEndpoint() == null || azure.getEndpoint().isBlank()
                || azure.getDeployment() == null || azure.getDeployment().isBlank()) {
            LOGGER.warn("Azure OpenAI provider selected but configuration is incomplete; mock fallback will be used");
        }
    }

    private void validateOllama() {
        var ollama = searchProperties.getSemantic().getOllama();
        if (ollama.getBaseUrl() == null || ollama.getBaseUrl().isBlank()) {
            LOGGER.warn("Ollama provider selected but base-url is missing; mock fallback will be used");
        }
    }

    private <T extends EmbeddingProvider> EmbeddingProvider resolveOrFallback(
            ObjectProvider<T> provider, String configured) {
        T resolved = provider.getIfAvailable();
        if (resolved == null || !isProviderConfigured(configured)) {
            return fallback("Provider misconfigured: " + configured);
        }
        LOGGER.info("Semantic embedding provider active: {}", configured);
        return resolved;
    }

    private boolean isProviderConfigured(String configured) {
        return switch (configured) {
            case "openai" -> hasOpenAiCredentials();
            case "azure-openai" -> hasAzureCredentials();
            case "ollama" -> hasOllamaBaseUrl();
            case "mock" -> true;
            default -> false;
        };
    }

    private boolean hasOpenAiCredentials() {
        String apiKey = searchProperties.getSemantic().getOpenai().getApiKey();
        return apiKey != null && !apiKey.isBlank();
    }

    private boolean hasAzureCredentials() {
        var azure = searchProperties.getSemantic().getAzure();
        return azure.getApiKey() != null && !azure.getApiKey().isBlank()
                && azure.getEndpoint() != null && !azure.getEndpoint().isBlank()
                && azure.getDeployment() != null && !azure.getDeployment().isBlank();
    }

    private boolean hasOllamaBaseUrl() {
        String baseUrl = searchProperties.getSemantic().getOllama().getBaseUrl();
        return baseUrl != null && !baseUrl.isBlank();
    }

    private EmbeddingProvider fallback(String reason) {
        LOGGER.warn("{}; using mock embedding provider", reason);
        MockEmbeddingProvider mock = mockProvider.getIfAvailable();
        return mock != null ? mock : new MockEmbeddingProvider();
    }

    private static String normalize(String provider) {
        if (provider == null || provider.isBlank()) {
            return "mock";
        }
        return provider.trim().toLowerCase(Locale.ROOT);
    }
}
