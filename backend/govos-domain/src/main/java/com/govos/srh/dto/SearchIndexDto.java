package com.govos.srh.dto;

import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;

import java.time.Instant;
import java.util.UUID;

public record SearchIndexDto(
        UUID id,
        String code,
        String name,
        String description,
        SearchEngineType engineType,
        SearchIndexStatus status,
        Integer mappingVersion,
        String physicalIndexName,
        String aliasName,
        Long activeDocumentCount,
        Instant lastReindexedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
