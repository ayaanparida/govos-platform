package com.govos.srh.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.OpenAiEmbeddingProperties;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAIEmbeddingProviderTest {

    private StubEmbeddingHttpTransport transport;
    private OpenAIEmbeddingProvider provider;

    @BeforeEach
    void setUp() {
        transport = new StubEmbeddingHttpTransport();
        OpenAiEmbeddingProperties properties = new OpenAiEmbeddingProperties();
        properties.setApiKey("test-key");
        properties.setModel("text-embedding-3-small");
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setDimension(3);
        provider = new OpenAIEmbeddingProvider(properties, transport, new ObjectMapper());
    }

    @Test
    void shouldGenerateEmbeddingFromOpenAiResponse() {
        transport.enqueue(HttpResponseStub.of(200, """
                {"data":[{"embedding":[0.1,0.2,0.3],"index":0}]}
                """));

        float[] embedding = provider.generateEmbedding("water leak");

        assertThat(embedding).containsExactly(0.1f, 0.2f, 0.3f);
        assertThat(transport.getCallCount()).isEqualTo(1);
        assertThat(provider.health()).isEqualTo(com.govos.srh.ai.EmbeddingHealthStatus.UP);
    }

    @Test
    void shouldWrapHttpFailuresInSemanticSearchException() {
        transport.enqueue(HttpResponseStub.of(500, "{\"error\":\"server\"}"));

        assertThatThrownBy(() -> provider.generateEmbedding("fail"))
                .isInstanceOf(com.govos.srh.ai.SemanticSearchException.class);
    }

    @Test
    void shouldExposeProviderMetadata() {
        assertThat(provider.providerName()).isEqualTo("openai");
        assertThat(provider.embeddingDimension()).isEqualTo(3);
        assertThat(provider.modelName()).isEqualTo("text-embedding-3-small");
    }
}
