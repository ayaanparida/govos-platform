package com.govos.doc.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentTagDto(
        UUID id,
        String code,
        String name,
        String description,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
