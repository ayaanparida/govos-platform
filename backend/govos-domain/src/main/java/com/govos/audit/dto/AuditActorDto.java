package com.govos.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditActorDto(
        UUID id,
        String code,
        UUID userId,
        String displayName,
        String organization,
        String department,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
