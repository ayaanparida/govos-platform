package com.govos.idm.dto;

import java.time.Instant;
import java.util.UUID;

public record LoginHistoryDto(
        UUID id,
        String code,
        UUID userId,
        Instant loginTime,
        Instant logoutTime,
        String ipAddress,
        String device,
        String browser,
        Boolean success,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
