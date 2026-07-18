package com.govos.srh.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.AzureOpenAiEmbeddingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AzureOpenAIEmbeddingProviderTest {

    private StubEmbeddingHttpTransport transport;
    private AzureOpenAIEmbeddingProvider provider;

    @BeforeEach
    void setUp() {
        transport = new StubEmbeddingHttpTransport();
        AzureOpenAiEmbeddingProperties properties = new AzureOpenAiEmbeddingProperties();
        properties.setApiKey("azure-key");
        properties.setEndpoint("https://example.openai.azure.com");
        properties.setDeployment("embed-deploy");
        properties.setDimension(3);
        provider = new AzureOpenAIEmbeddingProvider(properties, transport, new ObjectMapper());
    }

    @Test
    void shouldCallAzureEndpoint() {
        transport.enqueue(request -> {
            assertThat(request.uri().toString())
                    .contains("/openai/deployments/embed-deploy/embeddings");
            assertThat(request.headers().firstValue("api-key")).contains("azure-key");
            return HttpResponseStub.of(200, """
                    {"data":[{"embedding":[0.4,0.5,0.6],"index":0}]}
                    """);
        });

        float[] embedding = provider.generateEmbedding("road repair");

        assertThat(embedding).containsExactly(0.4f, 0.5f, 0.6f);
        assertThat(provider.providerName()).isEqualTo("azure-openai");
    }

    @Test
    void shouldReportDownWhenMisconfigured() {
        AzureOpenAiEmbeddingProperties properties = new AzureOpenAiEmbeddingProperties();
        AzureOpenAIEmbeddingProvider misconfigured =
                new AzureOpenAIEmbeddingProvider(properties, transport, new ObjectMapper());

        assertThat(misconfigured.health()).isEqualTo(com.govos.srh.ai.EmbeddingHealthStatus.DOWN);
    }

    @Test
    void shouldWrapFailures() {
        transport.enqueue(HttpResponseStub.of(401, "{}"));

        assertThatThrownBy(() -> provider.generateEmbedding("fail"))
                .isInstanceOf(com.govos.srh.ai.SemanticSearchException.class);
    }
}
