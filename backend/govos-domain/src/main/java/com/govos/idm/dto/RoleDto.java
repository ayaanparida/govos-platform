package com.govos.idm.dto;

import java.time.Instant;
import java.util.UUID;

public record RoleDto(
        UUID id,
        String code,
        String name,
        String description,
        Boolean systemRole,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
