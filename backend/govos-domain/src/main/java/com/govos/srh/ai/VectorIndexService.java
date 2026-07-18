package com.govos.srh.ai;

import java.util.List;
import java.util.UUID;

public interface VectorIndexService {

    void indexEmbedding(SearchEmbedding embedding);

    void indexEmbeddings(List<SearchEmbedding> embeddings);

    List<VectorSearchHit> search(UUID organizationId, float[] queryVector, int topK);

    long count();

    EmbeddingHealthStatus health();
}
