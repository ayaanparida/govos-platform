package com.govos.doc.storage.provider.gcs;

import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.testsupport.StorageTestFixtures;
import com.govos.doc.storage.port.StorageHealthStatus;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleCloudStorageProviderTest {

    @Mock private GoogleCloudStorageClient client;

    private GoogleCloudStorageProvider provider;

    @BeforeEach
    void setUp() {
        DocumentStorageProperties properties = new DocumentStorageProperties();
        provider = new GoogleCloudStorageProvider(client, properties, StorageTestFixtures.disabledMetrics(properties));
    }

    @Test
    void shouldDelegateStoreToClient() {
        StorageStoreRequest request = new StorageStoreRequest(
                UUID.randomUUID(), "bucket", "key", "text/plain", 4L, null);
        StorageStoreResult expected = new StorageStoreResult(new StorageObjectRef("bucket", "key"), null, 4L);
        when(client.putObject(any(), any())).thenReturn(expected);

        StorageStoreResult result = provider.store(request, new ByteArrayInputStream("data".getBytes()));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReportDegradedHealthWhenClientUnavailable() {
        when(client.ping()).thenReturn(false);

        assertThat(provider.health().status()).isEqualTo(StorageHealthStatus.DEGRADED);
    }
}
