package com.govos.idm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreatePasswordHistoryRequest(
        @NotNull
        UUID userId,
        @NotBlank @Size(max = 255)
        String passwordHash,
        @NotNull
        Instant changedDate,
        Boolean active
) {
}
