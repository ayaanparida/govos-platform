package com.govos.idm.dto;

import java.time.Instant;
import java.util.UUID;

public record RolePermissionDto(
        UUID id,
        String code,
        UUID roleId,
        UUID permissionId,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
