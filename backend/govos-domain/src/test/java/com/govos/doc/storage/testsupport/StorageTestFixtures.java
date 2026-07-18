package com.govos.doc.storage.testsupport;

import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.metrics.StorageMetricsRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;

public final class StorageTestFixtures {

    private StorageTestFixtures() {
    }

    public static StorageMetricsRecorder disabledMetrics(DocumentStorageProperties properties) {
        ObjectProvider<MeterRegistry> provider = new ObjectProvider<>() {
            @Override
            public MeterRegistry getObject(Object... args) {
                return null;
            }

            @Override
            public MeterRegistry getIfAvailable() {
                return null;
            }

            @Override
            public MeterRegistry getIfUnique() {
                return null;
            }
        };
        return new StorageMetricsRecorder(provider, properties);
    }
}
