package com.govos.srh.production;

import com.govos.srh.config.SearchProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SearchMetricsRecorder {

    private final MeterRegistry meterRegistry;
    private final boolean enabled;

    public SearchMetricsRecorder(ObjectProvider<MeterRegistry> meterRegistryProvider, SearchProperties searchProperties) {
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
        this.enabled = searchProperties.getMetrics().isEnabled() && meterRegistry != null;
    }

    public Timer.Sample startTimer() {
        return enabled ? Timer.start(meterRegistry) : null;
    }

    public void recordDuration(Timer.Sample sample, String metricName, String operation) {
        if (!enabled || sample == null) {
            return;
        }
        sample.stop(Timer.builder(metricName)
                .tag("operation", operation)
                .register(meterRegistry));
    }

    public void recordSearchRequest(String operation) {
        increment("search.requests", "operation", operation);
    }

    public void recordSearchError(String operation) {
        increment("search.errors", "operation", operation);
    }

    public void recordBulkOperation(long successCount, long failureCount) {
        increment("search.bulk.operations", "result", "attempt");
        if (failureCount > 0) {
            Counter.builder("bulk.failures")
                    .register(meterRegistry)
                    .increment(failureCount);
        }
        if (successCount > 0) {
            Counter.builder("search.bulk.operations")
                    .tag("result", "success")
                    .register(meterRegistry)
                    .increment(successCount);
        }
    }

    public void recordIndexOperation(String operation) {
        increment("search.index.operations", "operation", operation);
    }

    public void recordAliasSwitch() {
        increment("search.alias.switches", "operation", "switch");
    }

    public void recordClusterHealth(String status) {
        increment("cluster.health", "status", status);
    }

    public void recordSemanticRequest() {
        increment("semantic.requests", "operation", "semantic");
    }

    public void recordSemanticDuration(long durationMs) {
        if (!enabled) {
            return;
        }
        Timer.builder("semantic.duration")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordAutocompleteRequest() {
        increment("autocomplete.requests", "operation", "autocomplete");
    }

    public void recordFacetRequest() {
        increment("facet.requests", "operation", "facet");
    }

    public void recordGeoRequest() {
        increment("geo.requests", "operation", "geo");
    }

    public void recordEmbeddingRequest(String provider) {
        increment("embedding.requests", "provider", provider);
    }

    public void recordEmbeddingDuration(long durationMs, String provider) {
        if (!enabled) {
            return;
        }
        Timer.builder("embedding.duration")
                .tag("provider", provider)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordEmbeddingError(String provider) {
        increment("embedding.errors", "provider", provider);
    }

    public void recordProviderHealth(String provider, String status) {
        increment("provider.health", "provider", provider + ":" + status);
    }

    public void recordProviderCall(String provider, String result) {
        increment("provider.calls", "provider", provider + ":" + result);
    }

    public void recordProviderTokens(String provider, long tokens) {
        if (!enabled || tokens <= 0) {
            return;
        }
        Counter.builder("provider.tokens")
                .tag("provider", provider)
                .register(meterRegistry)
                .increment(tokens);
    }

    public void recordVectorIndexOperation(String operation) {
        increment("vector.index.operations", "operation", operation);
    }

    public void recordVectorSearchOperation(String operation) {
        increment("vector.search.operations", "operation", operation);
    }

    public void recordSchedulerExecution(String jobName) {
        increment("scheduler.executions", "job", jobName);
    }

    public void recordSchedulerDuration(long durationMs, String jobName) {
        if (!enabled) {
            return;
        }
        Timer.builder("scheduler.duration")
                .tag("job", jobName)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordSchedulerFailure(String jobName) {
        increment("scheduler.failures", "job", jobName);
    }

    public void recordSchedulerRetry(String jobName) {
        increment("scheduler.retries", "job", jobName);
    }

    public void recordSchedulerSkipped(String jobName) {
        increment("scheduler.skipped", "job", jobName);
    }

    private void increment(String metric, String tagKey, String tagValue) {
        if (!enabled) {
            return;
        }
        Counter.builder(metric)
                .tag(tagKey, tagValue)
                .register(meterRegistry)
                .increment();
    }
}
