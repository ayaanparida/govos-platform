package com.govos.doc.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionDto(
        UUID id,
        String code,
        UUID documentId,
        Integer versionNumber,
        String checksum,
        Long size,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
