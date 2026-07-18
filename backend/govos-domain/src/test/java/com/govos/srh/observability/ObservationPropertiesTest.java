package com.govos.srh.observability;

import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationPropertiesTest {

    @Test
    void shouldBindObservationProperties() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "govos.search.observation.enabled", "true",
                "govos.search.observation.exporter", "otlp",
                "govos.search.observation.otlp-endpoint", "http://collector:4317",
                "govos.search.observation.sample-rate", "0.5",
                "govos.search.observation.log-spans", "true",
                "govos.search.observation.log-events", "true",
                "govos.search.observation.trace-history-max-entries", "250"));

        SearchProperties properties = new SearchProperties();
        Binder binder = new Binder(source);
        binder.bind("govos.search", Bindable.ofInstance(properties));

        assertThat(properties.getObservation().isEnabled()).isTrue();
        assertThat(properties.getObservation().getExporter()).isEqualTo("otlp");
        assertThat(properties.getObservation().getOtlpEndpoint()).isEqualTo("http://collector:4317");
        assertThat(properties.getObservation().getSampleRate()).isEqualTo(0.5);
        assertThat(properties.getObservation().isLogSpans()).isTrue();
        assertThat(properties.getObservation().isLogEvents()).isTrue();
        assertThat(properties.getObservation().getTraceHistoryMaxEntries()).isEqualTo(250);
    }
}
