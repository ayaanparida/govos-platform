package com.govos.srh.ai;

import com.govos.srh.ai.provider.EmbeddingCache;
import com.govos.srh.ai.provider.EmbeddingProviderFactory;
import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticProviderInfoServiceTest {

    @Mock
    private EmbeddingProvider embeddingProvider;
    @Mock
    private EmbeddingProviderFactory embeddingProviderFactory;

    private SemanticProviderInfoService service;

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.getSemantic().setEmbeddingVersion(2);
        properties.getSemantic().setVectorStore("memory");
        EmbeddingCache cache = new EmbeddingCache(properties);

        when(embeddingProvider.providerName()).thenReturn("mock");
        when(embeddingProvider.embeddingDimension()).thenReturn(384);
        when(embeddingProvider.health()).thenReturn(EmbeddingHealthStatus.UP);
        when(embeddingProviderFactory.configuredProviderName()).thenReturn("mock");

        service = new SemanticProviderInfoService(
                embeddingProvider, embeddingProviderFactory, cache, properties);
    }

    @Test
    void shouldExposeProviderInfo() {
        var info = service.getProviderInfo();

        assertThat(info.activeProvider()).isEqualTo("mock");
        assertThat(info.embeddingVersion()).isEqualTo(2);
        assertThat(info.vectorStore()).isEqualTo("memory");
        assertThat(info.providerHealth()).isEqualTo("UP");
    }
}
