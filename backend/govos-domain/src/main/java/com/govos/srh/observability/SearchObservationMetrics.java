package com.govos.srh.observability;

import com.govos.srh.production.SearchMetricsRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SearchObservationMetrics {

    private final SearchMetricsRecorder metricsRecorder;
    private final MeterRegistry meterRegistry;
    private final boolean enabled;

    public SearchObservationMetrics(
            SearchMetricsRecorder metricsRecorder,
            ObjectProvider<MeterRegistry> meterRegistryProvider,
            SearchObservationProperties properties) {
        this.metricsRecorder = metricsRecorder;
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
        this.enabled = properties.isEnabled() && meterRegistry != null;
    }

    public void recordTraceCreated(String operation) {
        increment("search.trace.created", "operation", operation);
    }

    public void recordTraceCompleted(String operation) {
        increment("search.trace.completed", "operation", operation);
    }

    public void recordTraceFailed(String operation) {
        increment("search.trace.failed", "operation", operation);
    }

    public void recordSpanDuration(String operation, long durationMs) {
        if (!enabled) {
            return;
        }
        Timer.builder("search.span.duration")
                .tag("operation", operation)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordObservationEvent(String eventType) {
        increment("search.observation.events", "event", eventType);
    }

    public void recordSchedulerTrace(String jobName) {
        increment("scheduler.trace", "job", jobName);
    }

    public void recordEmbeddingTrace(String provider) {
        increment("embedding.trace", "provider", provider);
    }

    public void recordProviderTrace(String provider) {
        increment("provider.trace", "provider", provider);
    }

    public void recordVectorTrace(String operation) {
        increment("vector.trace", "operation", operation);
    }

    public SearchMetricsRecorder underlying() {
        return metricsRecorder;
    }

    private void increment(String metric, String tagKey, String tagValue) {
        if (!enabled) {
            return;
        }
        meterRegistry.counter(metric, tagKey, tagValue).increment();
    }
}
