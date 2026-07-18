package com.govos.srh.ai.provider;

import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.ai.MockEmbeddingProvider;
import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmbeddingProviderFactoryTest {

    @Test
    void shouldResolveMockProviderByDefault() {
        SearchProperties properties = new SearchProperties();
        properties.getSemantic().setProvider("mock");

        EmbeddingProviderFactory factory = new EmbeddingProviderFactory(
                properties,
                providerOf(new MockEmbeddingProvider()),
                emptyProvider(),
                emptyProvider(),
                emptyProvider());

        EmbeddingProvider provider = factory.resolveProvider();

        assertThat(provider.providerName()).isEqualTo("mock");
    }

    @Test
    void shouldFallbackToMockWhenOpenAiMisconfigured() {
        SearchProperties properties = new SearchProperties();
        properties.getSemantic().setProvider("openai");

        EmbeddingProviderFactory factory = new EmbeddingProviderFactory(
                properties,
                emptyProvider(),
                emptyProvider(),
                emptyProvider(),
                emptyProvider());

        EmbeddingProvider provider = factory.resolveProvider();

        assertThat(provider.providerName()).isEqualTo("mock");
    }

    @Test
    void shouldValidateConfigurationWithoutThrowing() {
        SearchProperties properties = new SearchProperties();
        properties.getSemantic().setProvider("azure-openai");

        EmbeddingProviderFactory factory = new EmbeddingProviderFactory(
                properties,
                emptyProvider(),
                emptyProvider(),
                emptyProvider(),
                emptyProvider());

        factory.validateConfiguration();
        assertThat(factory.configuredProviderName()).isEqualTo("azure-openai");
    }

    private static <T> ObjectProvider<T> providerOf(T value) {
        @SuppressWarnings("unchecked")
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private static <T> ObjectProvider<T> emptyProvider() {
        @SuppressWarnings("unchecked")
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return provider;
    }
}
