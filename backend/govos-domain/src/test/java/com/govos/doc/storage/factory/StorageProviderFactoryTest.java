package com.govos.doc.storage.factory;

import com.govos.doc.enums.StorageProviderType;
import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.port.StorageException;
import com.govos.doc.storage.port.StorageProviderPort;
import com.govos.doc.storage.provider.azure.AzureBlobStorageProvider;
import com.govos.doc.storage.provider.gcs.GoogleCloudStorageProvider;
import com.govos.doc.storage.provider.local.LocalStorageProvider;
import com.govos.doc.storage.provider.minio.MinioStorageProvider;
import com.govos.doc.storage.provider.s3.S3StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class StorageProviderFactoryTest {

    @Mock private LocalStorageProvider localStorageProvider;
    @Mock private MinioStorageProvider minioStorageProvider;
    @Mock private S3StorageProvider s3StorageProvider;
    @Mock private AzureBlobStorageProvider azureBlobStorageProvider;
    @Mock private GoogleCloudStorageProvider googleCloudStorageProvider;

    private DocumentStorageProperties properties;
    private StorageProviderFactory factory;

    @BeforeEach
    void setUp() {
        properties = new DocumentStorageProperties();
        factory = new StorageProviderFactory(
                properties,
                localStorageProvider,
                minioStorageProvider,
                s3StorageProvider,
                azureBlobStorageProvider,
                googleCloudStorageProvider);
        when(localStorageProvider.providerName()).thenReturn("local");
        when(localStorageProvider.providerType()).thenReturn(StorageProviderType.LOCAL);
        when(minioStorageProvider.providerType()).thenReturn(StorageProviderType.MINIO);
    }

    @Test
    void shouldResolveLocalProviderByDefault() {
        StorageProviderPort provider = factory.resolveActiveProvider();

        assertThat(provider).isSameAs(localStorageProvider);
    }

    @Test
    void shouldResolveProviderByName() {
        properties.setProvider("minio");

        StorageProviderPort provider = factory.resolveByName("minio");

        assertThat(provider).isSameAs(minioStorageProvider);
    }

    @Test
    void shouldRejectUnsupportedProvider() {
        assertThatThrownBy(() -> factory.resolveByName("unknown"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Unsupported storage provider");
    }

    @Test
    void shouldExposeConfiguredProviderName() {
        properties.setProvider("s3");

        assertThat(factory.configuredProviderName()).isEqualTo("s3");
    }
}
