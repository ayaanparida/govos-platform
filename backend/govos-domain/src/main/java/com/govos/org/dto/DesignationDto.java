package com.govos.org.dto;

import java.time.Instant;
import java.util.UUID;

public record DesignationDto(
        UUID id,
        String code,
        String title,
        String grade,
        String description,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
