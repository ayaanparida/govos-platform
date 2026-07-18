package com.govos.doc.dto.storage;

import com.govos.doc.enums.StorageProviderType;

public record UpdateStorageProviderRequest(
        String providerName,
        StorageProviderType providerType,
        String bucketName,
        String endpoint,
        String region,
        Boolean encryptionEnabled,
        Boolean isDefault,
        String secretKeyReference,
        Boolean active,
        Long version
) {
}
