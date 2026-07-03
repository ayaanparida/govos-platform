package com.govos.doc.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentTagMappingDto(
        UUID id,
        String code,
        UUID documentId,
        UUID tagId,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
