package com.govos.srh.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticSearchPropertiesBindingTest {

    @Test
    void shouldBindSemanticProviderConfiguration() {
        Map<String, Object> values = new HashMap<>();
        values.put("govos.search.semantic.provider", "openai");
        values.put("govos.search.semantic.vector-store", "opensearch");
        values.put("govos.search.semantic.openai.api-key", "secret");
        values.put("govos.search.semantic.openai.model", "text-embedding-3-small");
        values.put("govos.search.semantic.azure.deployment", "embed");
        values.put("govos.search.semantic.ollama.model", "nomic-embed-text");
        values.put("govos.search.semantic.embedding-cache.enabled", "true");

        Binder binder = new Binder(new MapConfigurationPropertySource(values));
        SearchProperties properties = binder
                .bind("govos.search", Bindable.of(SearchProperties.class))
                .orElseGet(SearchProperties::new);

        assertThat(properties.getSemantic().getProvider()).isEqualTo("openai");
        assertThat(properties.getSemantic().getVectorStore()).isEqualTo("opensearch");
        assertThat(properties.getSemantic().getOpenai().getApiKey()).isEqualTo("secret");
        assertThat(properties.getSemantic().getEmbeddingCache().isEnabled()).isTrue();
    }
}
