package com.govos.srh.ai.provider;

import com.govos.srh.ai.EmbeddingHealthStatus;
import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.ai.SemanticSearchException;
import com.govos.srh.production.SearchMetricsRecorder;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedEmbeddingProvider implements EmbeddingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger("com.govos.srh.embedding");

    private final EmbeddingProvider delegate;
    private final EmbeddingCache embeddingCache;
    private final SearchMetricsRecorder metricsRecorder;
    private final String modelName;

    public CachedEmbeddingProvider(
            EmbeddingProvider delegate,
            EmbeddingCache embeddingCache,
            SearchMetricsRecorder metricsRecorder,
            String modelName) {
        this.delegate = delegate;
        this.embeddingCache = embeddingCache;
        this.metricsRecorder = metricsRecorder;
        this.modelName = modelName != null ? modelName : delegate.providerName();
    }

    @Override
    public float[] generateEmbedding(String text) {
        long started = System.currentTimeMillis();
        metricsRecorder.recordEmbeddingRequest(delegate.providerName());
        try {
            float[] cached = embeddingCache.get(delegate.providerName(), modelName, text);
            if (cached != null) {
                metricsRecorder.recordProviderCall(delegate.providerName(), "cache-hit");
                return cached;
            }
            float[] embedding = delegate.generateEmbedding(text);
            embeddingCache.put(delegate.providerName(), modelName, text, embedding);
            metricsRecorder.recordEmbeddingDuration(System.currentTimeMillis() - started, delegate.providerName());
            logSuccess("generate", null, started);
            return embedding;
        } catch (RuntimeException ex) {
            metricsRecorder.recordEmbeddingError(delegate.providerName());
            logFailure("generate", null, started, ex);
            if (ex instanceof SemanticSearchException semanticSearchException) {
                throw semanticSearchException;
            }
            throw new SemanticSearchException("Embedding generation failed", ex);
        }
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        long started = System.currentTimeMillis();
        metricsRecorder.recordEmbeddingRequest(delegate.providerName());
        try {
            List<float[]> cached = embeddingCache.getBatch(delegate.providerName(), modelName, texts);
            if (cached != null) {
                metricsRecorder.recordProviderCall(delegate.providerName(), "cache-hit");
                return cached;
            }
            List<float[]> embeddings = delegate.generateEmbeddings(texts);
            embeddingCache.putBatch(delegate.providerName(), modelName, texts, embeddings);
            metricsRecorder.recordEmbeddingDuration(System.currentTimeMillis() - started, delegate.providerName());
            logSuccess("batch", texts.size(), started);
            return embeddings;
        } catch (RuntimeException ex) {
            metricsRecorder.recordEmbeddingError(delegate.providerName());
            logFailure("batch", texts.size(), started, ex);
            if (ex instanceof SemanticSearchException semanticSearchException) {
                throw semanticSearchException;
            }
            throw new SemanticSearchException("Batch embedding generation failed", ex);
        }
    }

    @Override
    public int embeddingDimension() {
        return delegate.embeddingDimension();
    }

    @Override
    public EmbeddingHealthStatus health() {
        EmbeddingHealthStatus status = delegate.health();
        metricsRecorder.recordProviderHealth(delegate.providerName(), status.name());
        return status;
    }

    @Override
    public String providerName() {
        return delegate.providerName();
    }

    EmbeddingProvider delegate() {
        return delegate;
    }

    private void logSuccess(String operation, Integer batchSize, long started) {
        LOGGER.info(
                "embedding_operation provider={} operation={} status=SUCCESS durationMs={} batchSize={}",
                delegate.providerName(),
                operation,
                System.currentTimeMillis() - started,
                batchSize != null ? batchSize : 1);
    }

    private void logFailure(String operation, Integer batchSize, long started, RuntimeException ex) {
        LOGGER.warn(
                "embedding_operation provider={} operation={} status=ERROR durationMs={} batchSize={} errorCode={}",
                delegate.providerName(),
                operation,
                System.currentTimeMillis() - started,
                batchSize != null ? batchSize : 1,
                ex.getClass().getSimpleName());
    }
}
