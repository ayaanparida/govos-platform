package com.govos.srh.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.ai.EmbeddingHealthStatus;
import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.ai.SemanticSearchException;
import com.govos.srh.config.OpenAiEmbeddingProperties;
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
@ConditionalOnProperty(name = "govos.search.semantic.provider", havingValue = "openai")
public class OpenAIEmbeddingProvider implements EmbeddingProvider {

    private final OpenAiEmbeddingProperties properties;
    private final EmbeddingHttpTransport transport;
    private final ObjectMapper objectMapper;
    private volatile EmbeddingHealthStatus lastHealth = EmbeddingHealthStatus.UNKNOWN;

    public OpenAIEmbeddingProvider(
            SearchProperties searchProperties,
            EmbeddingHttpTransport transport,
            ObjectMapper objectMapper) {
        this.properties = searchProperties.getSemantic().getOpenai();
        this.transport = transport;
        this.objectMapper = objectMapper;
    }

    OpenAIEmbeddingProvider(
            OpenAiEmbeddingProperties properties,
            EmbeddingHttpTransport transport,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.transport = transport;
        this.objectMapper = objectMapper;
    }

    @Override
    public float[] generateEmbedding(String text) {
        List<float[]> embeddings = generateEmbeddings(List.of(text));
        return embeddings.isEmpty() ? new float[embeddingDimension()] : embeddings.getFirst();
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        try {
            String payload = buildPayload(texts);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/embeddings"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = transport.send(request);
            if (response.statusCode() >= 400) {
                lastHealth = EmbeddingHealthStatus.DOWN;
                throw new SemanticSearchException(
                        "OpenAI embedding request failed with status " + response.statusCode());
            }
            lastHealth = EmbeddingHealthStatus.UP;
            return parseEmbeddings(response.body(), texts.size());
        } catch (SemanticSearchException ex) {
            throw ex;
        } catch (IOException | InterruptedException ex) {
            lastHealth = EmbeddingHealthStatus.DOWN;
            if (ex instanceof InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            throw new SemanticSearchException("OpenAI embedding request failed", ex);
        }
    }

    @Override
    public int embeddingDimension() {
        return properties.getDimension();
    }

    @Override
    public EmbeddingHealthStatus health() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return EmbeddingHealthStatus.DOWN;
        }
        return lastHealth;
    }

    @Override
    public String providerName() {
        return "openai";
    }

    String modelName() {
        return properties.getModel();
    }

    private String buildPayload(List<String> texts) throws IOException {
        if (texts.size() == 1) {
            return objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("model", properties.getModel())
                            .put("input", texts.getFirst()));
        }
        return objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                        .put("model", properties.getModel())
                        .set("input", objectMapper.valueToTree(texts)));
    }

    private List<float[]> parseEmbeddings(String body, int expectedCount) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) {
            throw new SemanticSearchException("OpenAI embedding response missing data array");
        }
        List<float[]> embeddings = new ArrayList<>(expectedCount);
        for (JsonNode item : data) {
            embeddings.add(toVector(item.get("embedding")));
        }
        return embeddings;
    }

    static float[] toVector(JsonNode embeddingNode) {
        if (embeddingNode == null || !embeddingNode.isArray()) {
            throw new SemanticSearchException("Embedding response missing vector");
        }
        float[] vector = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            vector[i] = (float) embeddingNode.get(i).asDouble();
        }
        return vector;
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
