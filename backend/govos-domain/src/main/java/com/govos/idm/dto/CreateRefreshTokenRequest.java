package com.govos.idm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateRefreshTokenRequest(
        @NotNull
        UUID userId,
        @NotBlank @Size(max = 500)
        String token,
        @NotNull
        Instant expiry,
        Boolean revoked,
        Boolean active
) {
}
