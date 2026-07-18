package com.govos.doc.storage.metrics;

import com.govos.doc.storage.config.DocumentStorageProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class StorageMetricsRecorder {

    private final MeterRegistry meterRegistry;
    private final boolean enabled;

    public StorageMetricsRecorder(
            ObjectProvider<MeterRegistry> meterRegistryProvider,
            DocumentStorageProperties properties) {
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
        this.enabled = properties.getMetrics().isEnabled() && meterRegistry != null;
    }

    public void recordUpload(String provider, long bytes) {
        increment("storage.uploads", provider);
        recordBytes("storage.bytes.uploaded", provider, bytes);
    }

    public void recordDownload(String provider, long bytes) {
        increment("storage.downloads", provider);
        recordBytes("storage.bytes.downloaded", provider, bytes);
    }

    public void recordDelete(String provider) {
        increment("storage.deletes", provider);
    }

    public void recordCopy(String provider) {
        increment("storage.copies", provider);
    }

    public void recordMove(String provider) {
        increment("storage.moves", provider);
    }

    public void recordFailure(String provider) {
        increment("storage.failures", provider);
    }

    private void increment(String metric, String provider) {
        if (!enabled) {
            return;
        }
        Counter.builder(metric)
                .tag("provider", provider)
                .register(meterRegistry)
                .increment();
    }

    private void recordBytes(String metric, String provider, long bytes) {
        if (!enabled || bytes <= 0) {
            return;
        }
        Counter.builder(metric)
                .tag("provider", provider)
                .register(meterRegistry)
                .increment(bytes);
    }
}
