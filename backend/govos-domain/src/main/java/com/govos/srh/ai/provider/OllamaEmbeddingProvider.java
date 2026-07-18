package com.govos.srh.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.ai.EmbeddingHealthStatus;
import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.ai.SemanticSearchException;
import com.govos.srh.config.OllamaEmbeddingProperties;
import com.govos.srh.config.SearchProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "govos.search.semantic.provider", havingValue = "ollama")
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private final OllamaEmbeddingProperties properties;
    private final EmbeddingHttpTransport transport;
    private final ObjectMapper objectMapper;
    private volatile EmbeddingHealthStatus lastHealth = EmbeddingHealthStatus.UNKNOWN;

    public OllamaEmbeddingProvider(
            SearchProperties searchProperties,
            EmbeddingHttpTransport transport,
            ObjectMapper objectMapper) {
        this.properties = searchProperties.getSemantic().getOllama();
        this.transport = transport;
        this.objectMapper = objectMapper;
    }

    OllamaEmbeddingProvider(
            OllamaEmbeddingProperties properties,
            EmbeddingHttpTransport transport,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.transport = transport;
        this.objectMapper = objectMapper;
    }

    @Override
    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return new float[embeddingDimension()];
        }
        try {
            String payload = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("model", properties.getModel())
                            .put("prompt", text));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/api/embeddings"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = transport.send(request);
            if (response.statusCode() >= 400) {
                lastHealth = EmbeddingHealthStatus.DOWN;
                throw new SemanticSearchException(
                        "Ollama embedding request failed with status " + response.statusCode());
            }
            lastHealth = EmbeddingHealthStatus.UP;
            JsonNode root = objectMapper.readTree(response.body());
            return OpenAIEmbeddingProvider.toVector(root.get("embedding"));
        } catch (SemanticSearchException ex) {
            throw ex;
        } catch (IOException | InterruptedException ex) {
            lastHealth = EmbeddingHealthStatus.DOWN;
            if (ex instanceof InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            throw new SemanticSearchException("Ollama embedding request failed", ex);
        }
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        List<float[]> embeddings = new ArrayList<>(texts.size());
        for (String text : texts) {
            embeddings.add(generateEmbedding(text));
        }
        return embeddings;
    }

    @Override
    public int embeddingDimension() {
        return properties.getDimension();
    }

    @Override
    public EmbeddingHealthStatus health() {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            return EmbeddingHealthStatus.DOWN;
        }
        return lastHealth;
    }

    @Override
    public String providerName() {
        return "ollama";
    }

    String modelName() {
        return properties.getModel();
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
