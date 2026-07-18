package com.govos.srh.production;

import com.govos.srh.config.SearchProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchMetricsRecorderTest {

    @Test
    void shouldRecordMetricsWhenEnabled() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        @SuppressWarnings("unchecked")
        ObjectProvider<io.micrometer.core.instrument.MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(registry);

        SearchProperties properties = new SearchProperties();
        SearchMetricsRecorder recorder = new SearchMetricsRecorder(provider, properties);

        recorder.recordSearchRequest("search");
        recorder.recordBulkOperation(3, 1);
        recorder.recordClusterHealth("UP");

        assertThat(registry.find("search.requests").counter()).isNotNull();
        assertThat(registry.find("bulk.failures").counter().count()).isEqualTo(1D);
        assertThat(registry.find("cluster.health").counter()).isNotNull();
    }

    @Test
    void shouldNoOpWhenMetricsDisabled() {
        SearchProperties properties = new SearchProperties();
        properties.getMetrics().setEnabled(false);
        @SuppressWarnings("unchecked")
        ObjectProvider<io.micrometer.core.instrument.MeterRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(new SimpleMeterRegistry());

        SearchMetricsRecorder recorder = new SearchMetricsRecorder(provider, properties);
        recorder.recordSearchRequest("search");
    }
}
