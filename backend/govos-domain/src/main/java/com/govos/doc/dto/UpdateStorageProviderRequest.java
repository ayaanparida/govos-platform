package com.govos.doc.dto;

import com.govos.doc.entity.StorageProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStorageProviderRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotNull
        StorageProviderType providerType,
        @NotBlank @Size(max = 255)
        String bucketName,
        @Size(max = 500)
        String endpoint,
        Boolean active,
        Long version
) {
}
