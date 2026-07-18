package com.govos.srh.ai.vector;

import com.govos.srh.ai.EmbeddingHealthStatus;
import com.govos.srh.ai.SearchEmbedding;
import com.govos.srh.ai.SemanticSearchException;
import com.govos.srh.ai.VectorIndexService;
import com.govos.srh.ai.VectorSearchHit;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.exception.SearchEngineException;
import com.govos.srh.production.SearchMetricsRecorder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(name = "govos.search.semantic.vector-store", havingValue = "opensearch")
public class OpenSearchVectorIndexService implements VectorIndexService {

    private static final String VECTOR_FIELD = "embedding_vector";

    private final OpenSearchClient openSearchClient;
    private final SearchProperties searchProperties;
    private final SearchMetricsRecorder metricsRecorder;
    private volatile EmbeddingHealthStatus healthStatus = EmbeddingHealthStatus.UNKNOWN;

    public OpenSearchVectorIndexService(
            OpenSearchClient openSearchClient,
            SearchProperties searchProperties,
            SearchMetricsRecorder metricsRecorder) {
        this.openSearchClient = openSearchClient;
        this.searchProperties = searchProperties;
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public void indexEmbedding(SearchEmbedding embedding) {
        ensureIndexExists();
        try {
            Map<String, Object> document = toDocument(embedding);
            openSearchClient.index(builder -> builder
                    .index(indexName())
                    .id(documentId(embedding))
                    .document(document)
                    .refresh(Refresh.False));
            metricsRecorder.recordVectorIndexOperation("index");
            healthStatus = EmbeddingHealthStatus.UP;
        } catch (Exception ex) {
            healthStatus = EmbeddingHealthStatus.DEGRADED;
            throw new SemanticSearchException("Failed to index embedding vector", ex);
        }
    }

    @Override
    public void indexEmbeddings(List<SearchEmbedding> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            return;
        }
        ensureIndexExists();
        try {
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
            for (SearchEmbedding embedding : embeddings) {
                bulkBuilder.operations(op -> op.index(idx -> idx
                        .index(indexName())
                        .id(documentId(embedding))
                        .document(toDocument(embedding))));
            }
            openSearchClient.bulk(bulkBuilder.build());
            metricsRecorder.recordVectorIndexOperation("bulk-index");
            healthStatus = EmbeddingHealthStatus.UP;
        } catch (Exception ex) {
            healthStatus = EmbeddingHealthStatus.DEGRADED;
            throw new SemanticSearchException("Failed to bulk index embedding vectors", ex);
        }
    }

    @Override
    public List<VectorSearchHit> search(UUID organizationId, float[] queryVector, int topK) {
        if (queryVector == null || queryVector.length == 0 || topK <= 0) {
            return List.of();
        }
        try {
            if (!indexExists()) {
                healthStatus = EmbeddingHealthStatus.DEGRADED;
                return List.of();
            }
            Query orgFilter = Query.of(fq -> fq.term(t -> t
                    .field("organizationId")
                    .value(v -> v.stringValue(organizationId.toString()))));
            Query knnQuery = Query.of(q -> q.knn(k -> k
                    .field(VECTOR_FIELD)
                    .vector(queryVector)
                    .k(topK)
                    .filter(orgFilter)));

            SearchRequest request = new SearchRequest.Builder()
                    .index(indexName())
                    .size(topK)
                    .query(knnQuery)
                    .build();
            var response = openSearchClient.search(request, Map.class);
            metricsRecorder.recordVectorSearchOperation("knn");
            healthStatus = EmbeddingHealthStatus.UP;

            List<VectorSearchHit> hits = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source == null) {
                    continue;
                }
                UUID referenceId = UUID.fromString(String.valueOf(source.get("referenceId")));
                UUID orgId = UUID.fromString(String.valueOf(source.get("organizationId")));
                hits.add(new VectorSearchHit(
                        referenceId.toString(),
                        referenceId,
                        orgId,
                        source.get("entityType") != null ? String.valueOf(source.get("entityType")) : null,
                        hit.score() != null ? hit.score() : 0D,
                        Map.of(
                                "entityType", source.get("entityType") != null ? source.get("entityType") : "",
                                "embeddingVersion", source.get("embeddingVersion") != null
                                        ? source.get("embeddingVersion")
                                        : searchProperties.getSemantic().getEmbeddingVersion())));
            }
            return hits;
        } catch (Exception ex) {
            healthStatus = EmbeddingHealthStatus.DOWN;
            throw new SemanticSearchException("Vector search failed", ex);
        }
    }

    @Override
    public long count() {
        try {
            if (!indexExists()) {
                return 0L;
            }
            CountRequest request = new CountRequest.Builder().index(indexName()).build();
            return openSearchClient.count(request).count();
        } catch (Exception ex) {
            healthStatus = EmbeddingHealthStatus.DEGRADED;
            return 0L;
        }
    }

    @Override
    public EmbeddingHealthStatus health() {
        try {
            if (!indexExists()) {
                return EmbeddingHealthStatus.DEGRADED;
            }
            return healthStatus == EmbeddingHealthStatus.UNKNOWN ? EmbeddingHealthStatus.UP : healthStatus;
        } catch (Exception ex) {
            return EmbeddingHealthStatus.DOWN;
        }
    }

    public void deleteEmbedding(UUID referenceId, int embeddingVersion) {
        try {
            if (!indexExists()) {
                return;
            }
            DeleteRequest request = new DeleteRequest.Builder()
                    .index(indexName())
                    .id(referenceId + "-v" + embeddingVersion)
                    .build();
            openSearchClient.delete(request);
            metricsRecorder.recordVectorIndexOperation("delete");
        } catch (Exception ex) {
            throw new SemanticSearchException("Failed to delete embedding vector", ex);
        }
    }

    public void deleteEmbeddingsForVersion(int embeddingVersion) {
        // Version-scoped cleanup is performed during migration rollback via document ids.
        metricsRecorder.recordVectorIndexOperation("delete-version");
    }

    private void ensureIndexExists() {
        if (indexExists()) {
            return;
        }
        try {
            int dimension = searchProperties.getSemantic().getVectorDimension();
            CreateIndexRequest request = new CreateIndexRequest.Builder()
                    .index(indexName())
                    .settings(IndexSettings.of(settings -> settings
                            .knn(true)
                            .numberOfShards("1")
                            .numberOfReplicas("0")))
                    .mappings(TypeMapping.of(mapping -> mapping
                            .properties("referenceId", Property.of(p -> p.keyword(k -> k)))
                            .properties("organizationId", Property.of(p -> p.keyword(k -> k)))
                            .properties("entityType", Property.of(p -> p.keyword(k -> k)))
                            .properties("embeddingVersion", Property.of(p -> p.integer(i -> i)))
                            .properties("vectorDimension", Property.of(p -> p.integer(i -> i)))
                            .properties(VECTOR_FIELD, Property.of(p -> p.knnVector(k -> k
                                    .dimension(dimension)
                                    .method(m -> m.name("hnsw")
                                            .spaceType("cosinesimil")
                                            .engine("nmslib")))))))
                    .build();
            openSearchClient.indices().create(request);
            metricsRecorder.recordVectorIndexOperation("create-index");
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to create vector index: " + indexName(), ex);
        }
    }

    private boolean indexExists() {
        try {
            ExistsRequest request = new ExistsRequest.Builder().index(indexName()).build();
            return openSearchClient.indices().exists(request).value();
        } catch (Exception ex) {
            return false;
        }
    }

    private String indexName() {
        return searchProperties.getSemantic().getVectorIndexName();
    }

    private static String documentId(SearchEmbedding embedding) {
        int version = embedding.getEmbeddingVersion() != null
                ? embedding.getEmbeddingVersion()
                : 1;
        return embedding.getReferenceId() + "-v" + version;
    }

    private Map<String, Object> toDocument(SearchEmbedding embedding) {
        Map<String, Object> document = new HashMap<>();
        document.put("embeddingId", embedding.getEmbeddingId() != null
                ? embedding.getEmbeddingId().toString()
                : UUID.randomUUID().toString());
        document.put("referenceId", embedding.getReferenceId().toString());
        document.put("organizationId", embedding.getOrganizationId().toString());
        document.put("entityType", embedding.getEntityType());
        document.put("embeddingVersion", embedding.getEmbeddingVersion() != null
                ? embedding.getEmbeddingVersion()
                : searchProperties.getSemantic().getEmbeddingVersion());
        document.put("vectorDimension", embedding.getVectorDimension());
        document.put(VECTOR_FIELD, toFloatList(embedding.getVector()));
        document.put("updatedDate", Instant.now().toString());
        return document;
    }

    private static List<Float> toFloatList(float[] vector) {
        List<Float> values = new ArrayList<>(vector.length);
        for (float value : vector) {
            values.add(value);
        }
        return values;
    }
}
