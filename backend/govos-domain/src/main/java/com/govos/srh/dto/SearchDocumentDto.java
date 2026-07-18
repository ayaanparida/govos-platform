package com.govos.srh.dto;

import com.govos.srh.enums.SearchDocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record SearchDocumentDto(
        UUID id,
        String code,
        UUID searchIndexId,
        UUID searchDocumentId,
        String entityType,
        UUID referenceId,
        String referenceCode,
        UUID organizationId,
        String documentJson,
        String searchText,
        SearchDocumentStatus documentStatus,
        Long searchVersion,
        Instant indexedAt,
        Instant lastIndexedAt,
        UUID metadataOrganizationId,
        String metadataEntityType,
        UUID metadataReferenceId,
        String metadataReferenceCode,
        Integer metadataMappingVersion,
        Instant metadataIndexedAt,
        Instant metadataLastIndexedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
