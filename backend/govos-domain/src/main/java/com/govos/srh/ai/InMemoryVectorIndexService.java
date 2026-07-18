package com.govos.srh.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "govos.search.semantic.vector-store", havingValue = "memory", matchIfMissing = true)
public class InMemoryVectorIndexService implements VectorIndexService {

    private final Map<UUID, SearchEmbedding> embeddings = new ConcurrentHashMap<>();

    @Override
    public void indexEmbedding(SearchEmbedding embedding) {
        if (embedding.getEmbeddingId() == null) {
            embedding.setEmbeddingId(UUID.randomUUID());
        }
        embeddings.put(embedding.getEmbeddingId(), embedding);
    }

    @Override
    public void indexEmbeddings(List<SearchEmbedding> embeddingList) {
        if (embeddingList == null) {
            return;
        }
        for (SearchEmbedding embedding : embeddingList) {
            indexEmbedding(embedding);
        }
    }

    @Override
    public List<VectorSearchHit> search(UUID organizationId, float[] queryVector, int topK) {
        if (queryVector == null || queryVector.length == 0 || topK <= 0) {
            return List.of();
        }

        List<VectorSearchHit> hits = new ArrayList<>();
        for (SearchEmbedding embedding : embeddings.values()) {
            if (organizationId != null && !organizationId.equals(embedding.getOrganizationId())) {
                continue;
            }
            if (embedding.getVector() == null || embedding.getVector().length != queryVector.length) {
                continue;
            }
            double similarity = cosineSimilarity(queryVector, embedding.getVector());
            hits.add(new VectorSearchHit(
                    embedding.getReferenceId().toString(),
                    embedding.getReferenceId(),
                    embedding.getOrganizationId(),
                    embedding.getEntityType(),
                    similarity,
                    Map.of(
                            "entityType", embedding.getEntityType() != null ? embedding.getEntityType() : "",
                            "embeddingVersion", embedding.getEmbeddingVersion() != null
                                    ? embedding.getEmbeddingVersion()
                                    : 1)));
        }

        hits.sort(Comparator.comparingDouble(VectorSearchHit::similarityScore).reversed());
        if (hits.size() <= topK) {
            return hits;
        }
        return hits.subList(0, topK);
    }

    @Override
    public long count() {
        return embeddings.size();
    }

    @Override
    public EmbeddingHealthStatus health() {
        return EmbeddingHealthStatus.UP;
    }

    static double cosineSimilarity(float[] left, float[] right) {
        double dot = 0D;
        double leftMagnitude = 0D;
        double rightMagnitude = 0D;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftMagnitude += left[i] * left[i];
            rightMagnitude += right[i] * right[i];
        }
        if (leftMagnitude == 0D || rightMagnitude == 0D) {
            return 0D;
        }
        return dot / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
    }
}
