package com.govos.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditChangeDto(
        UUID id,
        String code,
        UUID auditEventId,
        String fieldName,
        String oldValue,
        String newValue,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
