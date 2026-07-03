package com.govos.wrk.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkflowDefinitionDto(
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
