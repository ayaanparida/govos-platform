package com.govos.idm.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRolePermissionRequest(
        @NotNull
        UUID roleId,
        @NotNull
        UUID permissionId,
        Boolean active
) {
}
