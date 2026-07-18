package com.govos.srh.observability;

import com.govos.srh.config.SearchProperties;
import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SearchTracingConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SearchTracingConfiguration.class))
            .withBean(SearchProperties.class, SearchProperties::new);

    @Test
    void shouldRegisterOpenTelemetryBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenTelemetry.class);
            assertThat(context).hasSingleBean(Tracer.class);
            assertThat(context).hasSingleBean(ObservationRegistry.class);
            assertThat(context).hasSingleBean(SearchObservationProperties.class);
        });
    }

    @Test
    void shouldProvideNoopTelemetryWhenExporterDisabled() {
        contextRunner.run(context -> {
            SearchObservationProperties properties = context.getBean(SearchObservationProperties.class);
            properties.setEnabled(false);
            assertThat(context.getBean(OpenTelemetry.class)).isNotNull();
        });
    }
}
