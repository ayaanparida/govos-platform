package com.govos.idm.dto;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenDto(
        UUID id,
        String code,
        UUID userId,
        String token,
        Instant expiry,
        Boolean revoked,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
