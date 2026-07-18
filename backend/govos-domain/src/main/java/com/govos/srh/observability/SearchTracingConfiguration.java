package com.govos.srh.observability;

import com.govos.srh.config.SearchProperties;
import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchTracingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SearchObservationProperties searchObservationProperties(SearchProperties searchProperties) {
        return searchProperties.getObservation();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry searchOpenTelemetry(SearchObservationProperties properties) {
        if (!properties.isEnabled() || !"otlp".equalsIgnoreCase(properties.getExporter())) {
            return OpenTelemetry.noop();
        }
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(properties.getOtlpEndpoint())
                .build();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        io.opentelemetry.api.common.AttributeKey.stringKey("service.name"), "govos-srh")));
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
                .build();
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "searchTracer")
    public Tracer searchTracer(OpenTelemetry searchOpenTelemetry) {
        return searchOpenTelemetry.getTracer("com.govos.srh");
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationRegistry searchObservationRegistry() {
        return ObservationRegistry.create();
    }
}
