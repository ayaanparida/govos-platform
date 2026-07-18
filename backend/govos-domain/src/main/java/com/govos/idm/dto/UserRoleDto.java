package com.govos.idm.dto;

import java.time.Instant;
import java.util.UUID;

public record UserRoleDto(
        UUID id,
        String code,
        UUID userId,
        UUID roleId,
        Instant assignedDate,
        Instant expiryDate,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
