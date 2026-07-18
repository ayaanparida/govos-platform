package com.govos.doc.dto.storage;

import com.govos.doc.enums.StorageProviderType;

import java.util.UUID;

public record StorageProviderResponse(
        UUID id,
        String code,
        String providerName,
        StorageProviderType providerType,
        String bucketName,
        String endpoint,
        String region,
        Boolean encryptionEnabled,
        Boolean isDefault,
        Boolean active,
        Long version
) {
}
