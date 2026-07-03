package com.govos.idm.dto;

import java.time.Instant;
import java.util.UUID;

public record PasswordHistoryDto(
        UUID id,
        String code,
        UUID userId,
        String passwordHash,
        Instant changedDate,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
