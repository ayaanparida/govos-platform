package com.govos.doc.storage.service;

import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.factory.StorageProviderFactory;
import com.govos.doc.storage.port.StorageException;
import com.govos.doc.storage.port.StorageHealth;
import com.govos.doc.storage.port.StorageHealthStatus;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageProviderPort;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class DocumentStorageServiceTest {

    private static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock private StorageProviderFactory storageProviderFactory;
    @Mock private StorageProviderPort storageProviderPort;

    private DocumentStorageProperties properties;
    private DocumentStorageService documentStorageService;

    @BeforeEach
    void setUp() {
        properties = new DocumentStorageProperties();
        properties.setMaxFileSize(1024L);
        documentStorageService = new DocumentStorageServiceImpl(storageProviderFactory, properties);
        when(storageProviderFactory.resolveActiveProvider()).thenReturn(storageProviderPort);
        when(storageProviderPort.providerName()).thenReturn("local");
    }

    @Test
    void shouldDelegateStoreToActiveProvider() {
        StorageStoreRequest request = new StorageStoreRequest(
                DOCUMENT_ID, "bucket", "key", "text/plain", 10L, null);
        StorageStoreResult expected = new StorageStoreResult(
                new StorageObjectRef("bucket", "key"),
                null,
                10L);
        when(storageProviderPort.store(any(), any())).thenReturn(expected);

        StorageStoreResult result = documentStorageService.storeDocument(
                request, new ByteArrayInputStream("hello".getBytes()));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldRejectOversizedUpload() {
        StorageStoreRequest request = new StorageStoreRequest(
                DOCUMENT_ID, "bucket", "key", "text/plain", 2048L, null);

        assertThatThrownBy(() -> documentStorageService.storeDocument(
                        request, new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("max-file-size");
    }

    @Test
    void shouldDelegateHealthCheck() {
        when(storageProviderPort.health()).thenReturn(StorageHealth.up());

        StorageHealth health = documentStorageService.health();

        assertThat(health.status()).isEqualTo(StorageHealthStatus.UP);
    }

    @Test
    void shouldUseConfiguredSignedUrlExpiration() {
        StorageObjectRef ref = new StorageObjectRef("bucket", "key");
        when(storageProviderPort.generateSignedDownloadUrl(ref, Duration.ofSeconds(300)))
                .thenReturn(new com.govos.doc.storage.port.SignedUrlResult("url", null, "GET"));

        documentStorageService.generateSignedDownloadUrl(ref, null);

        verify(storageProviderPort).generateSignedDownloadUrl(ref, Duration.ofSeconds(300));
    }
}
