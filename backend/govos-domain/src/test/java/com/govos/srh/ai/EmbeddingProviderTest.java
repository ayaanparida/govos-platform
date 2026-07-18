package com.govos.srh.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingProviderTest {

    private final MockEmbeddingProvider provider = new MockEmbeddingProvider(384);

    @Test
    void shouldGenerateDeterministicEmbedding() {
        float[] first = provider.generateEmbedding("water leak complaint");
        float[] second = provider.generateEmbedding("water leak complaint");

        assertThat(first).hasSize(384);
        assertThat(first).containsExactly(second);
    }

    @Test
    void shouldGenerateDifferentEmbeddingsForDifferentText() {
        float[] first = provider.generateEmbedding("water leak");
        float[] second = provider.generateEmbedding("road repair");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldGenerateBatchEmbeddings() {
        List<float[]> embeddings = provider.generateEmbeddings(List.of("alpha", "beta"));

        assertThat(embeddings).hasSize(2);
        assertThat(embeddings.get(0)).hasSize(384);
        assertThat(embeddings.get(1)).hasSize(384);
    }

    @Test
    void shouldExposeProviderMetadata() {
        assertThat(provider.embeddingDimension()).isEqualTo(384);
        assertThat(provider.providerName()).isEqualTo("mock");
        assertThat(provider.health()).isEqualTo(EmbeddingHealthStatus.UP);
    }

    @Test
    void shouldProduceNormalizedVectors() {
        float[] embedding = provider.generateEmbedding("normalized vector test");

        double magnitude = 0D;
        for (float value : embedding) {
            magnitude += value * value;
        }

        assertThat(magnitude).isCloseTo(1D, org.assertj.core.data.Offset.offset(0.0001));
    }
}
