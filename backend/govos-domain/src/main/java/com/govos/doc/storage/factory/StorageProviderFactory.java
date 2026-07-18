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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class StorageProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(StorageProviderFactory.class);

    private final DocumentStorageProperties properties;
    private final Map<StorageProviderType, StorageProviderPort> providersByType;

    public StorageProviderFactory(
            DocumentStorageProperties properties,
            LocalStorageProvider localStorageProvider,
            MinioStorageProvider minioStorageProvider,
            S3StorageProvider s3StorageProvider,
            AzureBlobStorageProvider azureBlobStorageProvider,
            GoogleCloudStorageProvider googleCloudStorageProvider) {
        this.properties = properties;
        this.providersByType = Map.of(
                StorageProviderType.LOCAL, localStorageProvider,
                StorageProviderType.MINIO, minioStorageProvider,
                StorageProviderType.S3, s3StorageProvider,
                StorageProviderType.AZURE_BLOB, azureBlobStorageProvider,
                StorageProviderType.GOOGLE_CLOUD_STORAGE, googleCloudStorageProvider);
    }

    public StorageProviderPort resolveActiveProvider() {
        return resolveByName(properties.getProvider());
    }

    public StorageProviderPort resolveByName(String providerName) {
        StorageProviderType type = mapProviderName(providerName);
        StorageProviderPort provider = providersByType.get(type);
        if (provider == null) {
            throw new StorageException("Unsupported storage provider: " + providerName);
        }
        log.debug("Resolved storage provider: {}", provider.providerName());
        return provider;
    }

    public StorageProviderPort resolveByType(StorageProviderType providerType) {
        StorageProviderPort provider = providersByType.get(providerType);
        if (provider == null) {
            throw new StorageException("Unsupported storage provider type: " + providerType);
        }
        return provider;
    }

    public String configuredProviderName() {
        return normalize(properties.getProvider());
    }

    private StorageProviderType mapProviderName(String providerName) {
        return switch (normalize(providerName)) {
            case "local" -> StorageProviderType.LOCAL;
            case "minio" -> StorageProviderType.MINIO;
            case "s3" -> StorageProviderType.S3;
            case "azure" -> StorageProviderType.AZURE_BLOB;
            case "gcs" -> StorageProviderType.GOOGLE_CLOUD_STORAGE;
            default -> throw new StorageException("Unsupported storage provider: " + providerName);
        };
    }

    private static String normalize(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return "local";
        }
        return providerName.trim().toLowerCase(Locale.ROOT);
    }
}
