package com.govos.srh.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.OllamaEmbeddingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OllamaEmbeddingProviderTest {

    private StubEmbeddingHttpTransport transport;
    private OllamaEmbeddingProvider provider;

    @BeforeEach
    void setUp() {
        transport = new StubEmbeddingHttpTransport();
        OllamaEmbeddingProperties properties = new OllamaEmbeddingProperties();
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("nomic-embed-text");
        properties.setDimension(3);
        provider = new OllamaEmbeddingProvider(properties, transport, new ObjectMapper());
    }

    @Test
    void shouldGenerateEmbeddingFromOllamaResponse() {
        transport.enqueue(HttpResponseStub.of(200, """
                {"embedding":[0.7,0.8,0.9]}
                """));

        float[] embedding = provider.generateEmbedding("complaint text");

        assertThat(embedding).containsExactly(0.7f, 0.8f, 0.9f);
        assertThat(provider.providerName()).isEqualTo("ollama");
    }

    @Test
    void shouldWrapHttpFailures() {
        transport.enqueue(HttpResponseStub.of(503, "{}"));

        assertThatThrownBy(() -> provider.generateEmbedding("fail"))
                .isInstanceOf(com.govos.srh.ai.SemanticSearchException.class);
    }
}
