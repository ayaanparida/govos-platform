package com.govos.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditEntityDto(
        UUID id,
        String code,
        String entityType,
        UUID entityId,
        String entityName,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
