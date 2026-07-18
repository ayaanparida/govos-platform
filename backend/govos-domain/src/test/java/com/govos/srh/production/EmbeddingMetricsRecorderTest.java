package com.govos.srh.production;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmbeddingMetricsRecorderTest {

    @Test
    void shouldRecordEmbeddingMetrics() {
        SearchProperties properties = new SearchProperties();
        properties.getMetrics().setEnabled(true);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        @SuppressWarnings("unchecked")
        ObjectProvider<io.micrometer.core.instrument.MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(registry);
        SearchMetricsRecorder recorder = new SearchMetricsRecorder(provider, properties);

        recorder.recordEmbeddingRequest("openai");
        recorder.recordEmbeddingDuration(25L, "openai");
        recorder.recordEmbeddingError("openai");
        recorder.recordProviderHealth("openai", "UP");
        recorder.recordProviderCall("openai", "success");
        recorder.recordProviderTokens("openai", 10);
        recorder.recordVectorIndexOperation("index");
        recorder.recordVectorSearchOperation("knn");

        assertThat(registry.find("embedding.requests").counter()).isNotNull();
        assertThat(registry.find("embedding.duration").timer()).isNotNull();
        assertThat(registry.find("embedding.errors").counter()).isNotNull();
        assertThat(registry.find("provider.health").counter()).isNotNull();
        assertThat(registry.find("provider.calls").counter()).isNotNull();
        assertThat(registry.find("provider.tokens").counter()).isNotNull();
        assertThat(registry.find("vector.index.operations").counter()).isNotNull();
        assertThat(registry.find("vector.search.operations").counter()).isNotNull();
    }
}
