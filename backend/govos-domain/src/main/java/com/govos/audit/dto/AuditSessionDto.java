package com.govos.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditSessionDto(
        UUID id,
        String code,
        String sessionId,
        Instant loginTime,
        Instant logoutTime,
        String ipAddress,
        String device,
        String browser,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
