package com.govos.srh.ai;

import java.util.List;

/**
 * Abstraction for AI embedding generation. Implementations may target OpenAI, Azure OpenAI,
 * Ollama, SentenceTransformers, Bedrock, Vertex AI, or other providers.
 */
public interface EmbeddingProvider {

    float[] generateEmbedding(String text);

    List<float[]> generateEmbeddings(List<String> texts);

    int embeddingDimension();

    EmbeddingHealthStatus health();

    String providerName();
}
