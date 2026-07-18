package com.govos.srh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record SearchDocumentUpdateRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 100)
        String entityType,
        @NotNull
        UUID referenceId,
        @Size(max = 100)
        String referenceCode,
        @NotNull
        UUID organizationId,
        String documentJson,
        String searchText,
        @PositiveOrZero
        Long searchVersion,
        Instant indexedAt,
        Instant lastIndexedAt,
        UUID metadataOrganizationId,
        @Size(max = 100)
        String metadataEntityType,
        UUID metadataReferenceId,
        @Size(max = 100)
        String metadataReferenceCode,
        Integer metadataMappingVersion,
        Instant metadataIndexedAt,
        Instant metadataLastIndexedAt,
        Boolean active,
        @NotNull
        Long version
) {
}
