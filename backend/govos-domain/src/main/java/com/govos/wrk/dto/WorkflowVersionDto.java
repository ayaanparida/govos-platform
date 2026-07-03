package com.govos.wrk.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkflowVersionDto(
        UUID id,
        String code,
        UUID definitionId,
        Integer versionNumber,
        Boolean published,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
