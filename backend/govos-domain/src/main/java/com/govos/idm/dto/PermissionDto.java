package com.govos.idm.dto;

import java.time.Instant;
import java.util.UUID;

public record PermissionDto(
        UUID id,
        String code,
        String module,
        String resource,
        String action,
        String description,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
