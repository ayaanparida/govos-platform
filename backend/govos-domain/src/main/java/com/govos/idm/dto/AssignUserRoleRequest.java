package com.govos.idm.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record AssignUserRoleRequest(
        @NotNull
        UUID userId,
        @NotNull
        UUID roleId,
        @NotNull
        Instant assignedDate,
        Instant expiryDate,
        Boolean active
) {
}
