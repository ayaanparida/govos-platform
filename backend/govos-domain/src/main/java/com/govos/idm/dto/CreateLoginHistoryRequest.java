package com.govos.idm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateLoginHistoryRequest(
        @NotNull
        UUID userId,
        @NotNull
        Instant loginTime,
        Instant logoutTime,
        @Size(max = 45)
        String ipAddress,
        @Size(max = 200)
        String device,
        @Size(max = 200)
        String browser,
        @NotNull
        Boolean success,
        Boolean active
) {
}
