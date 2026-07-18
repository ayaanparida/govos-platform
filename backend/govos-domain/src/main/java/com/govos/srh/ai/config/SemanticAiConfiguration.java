package com.govos.srh.ai.config;

import com.govos.srh.ai.provider.EmbeddingProviderFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class SemanticAiConfiguration {

    private final EmbeddingProviderFactory embeddingProviderFactory;

    public SemanticAiConfiguration(EmbeddingProviderFactory embeddingProviderFactory) {
        this.embeddingProviderFactory = embeddingProviderFactory;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateSemanticProviderOnStartup() {
        embeddingProviderFactory.validateConfiguration();
    }
}
