package com.govos.srh.production;

import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchProductionPropertiesTest {

    @Test
    void shouldExposeProductionDefaults() {
        SearchProperties properties = new SearchProperties();

        assertThat(properties.getResilience().getMaxRetries()).isEqualTo(3);
        assertThat(properties.getCache().isEnabled()).isTrue();
        assertThat(properties.getPool().getMaxConnections()).isEqualTo(50);
        assertThat(properties.getGuard().getMaxResultWindow()).isEqualTo(10000);
        assertThat(properties.getMetrics().isEnabled()).isTrue();
        assertThat(properties.getSemantic().getProvider()).isEqualTo("mock");
    }
}
