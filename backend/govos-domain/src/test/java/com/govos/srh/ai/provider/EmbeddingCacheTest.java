package com.govos.srh.ai.provider;

import com.govos.srh.ai.MockEmbeddingProvider;
import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingCacheTest {

    private EmbeddingCache cache;

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.getSemantic().getEmbeddingCache().setEnabled(true);
        properties.getSemantic().getEmbeddingCache().setMaxEntries(100);
        properties.getSemantic().getEmbeddingCache().setTtlSeconds(60);
        cache = new EmbeddingCache(properties);
    }

    @Test
    void shouldCacheEmbeddingsByProviderModelAndTextHash() {
        float[] vector = {0.1f, 0.2f};

        cache.put("mock", "model-a", "hello", vector);

        assertThat(cache.get("mock", "model-a", "hello")).containsExactly(0.1f, 0.2f);
        assertThat(cache.get("mock", "model-b", "hello")).isNull();
        assertThat(cache.get("openai", "model-a", "hello")).isNull();
    }

    @Test
    void shouldSupportBatchCacheHits() {
        float[] first = {0.1f, 0.2f, 0.3f};
        float[] second = {0.4f, 0.5f, 0.6f};
        cache.put("mock", "model", "alpha", first);
        cache.put("mock", "model", "beta", second);

        assertThat(cache.getBatch("mock", "model", java.util.List.of("alpha", "beta")))
                .containsExactly(first, second);
    }

    @Test
    void shouldEvictAllEntries() {
        cache.put("mock", "model", "text", new float[] {1f});
        cache.evictAll();
        assertThat(cache.size()).isZero();
    }
}
