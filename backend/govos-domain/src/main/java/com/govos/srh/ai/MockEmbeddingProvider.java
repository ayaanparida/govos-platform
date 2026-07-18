package com.govos.srh.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic mock embedding provider for development and testing.
 * Produces normalized vectors from text hashes — no external AI service required.
 */
@Component
@ConditionalOnProperty(name = "govos.search.semantic.provider", havingValue = "mock", matchIfMissing = true)
public class MockEmbeddingProvider implements EmbeddingProvider {

    static final int DEFAULT_DIMENSION = 384;

    private final int dimension;

    public MockEmbeddingProvider() {
        this(DEFAULT_DIMENSION);
    }

    MockEmbeddingProvider(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return zeroVector();
        }
        return normalizeVector(hashToVector(text));
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
        return dimension;
    }

    @Override
    public EmbeddingHealthStatus health() {
        return EmbeddingHealthStatus.UP;
    }

    @Override
    public String providerName() {
        return "mock";
    }

    private float[] hashToVector(String text) {
        byte[] digest = sha256(text);
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            int byteIndex = i % digest.length;
            vector[i] = (digest[byteIndex] & 0xFF) / 255.0f;
        }
        return vector;
    }

    private static float[] normalizeVector(float[] vector) {
        double magnitude = 0D;
        for (float value : vector) {
            magnitude += value * value;
        }
        magnitude = Math.sqrt(magnitude);
        if (magnitude == 0D) {
            return vector;
        }
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = (float) (vector[i] / magnitude);
        }
        return normalized;
    }

    private float[] zeroVector() {
        return new float[dimension];
    }

    private static byte[] sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new SemanticSearchException("Failed to generate mock embedding", ex);
        }
    }
}
