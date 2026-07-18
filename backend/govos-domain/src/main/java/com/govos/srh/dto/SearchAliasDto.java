package com.govos.srh.dto;

import java.time.Instant;
import java.util.UUID;

public record SearchAliasDto(
        UUID id,
        String code,
        UUID searchIndexId,
        String aliasName,
        String physicalIndexName,
        Boolean activeAlias,
        Instant switchedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
