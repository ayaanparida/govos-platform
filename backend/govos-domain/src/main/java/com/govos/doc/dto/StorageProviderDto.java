package com.govos.doc.dto;

import com.govos.doc.entity.StorageProviderType;

import java.time.Instant;
import java.util.UUID;

public record StorageProviderDto(
        UUID id,
        String code,
        StorageProviderType providerType,
        String bucketName,
        String endpoint,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
