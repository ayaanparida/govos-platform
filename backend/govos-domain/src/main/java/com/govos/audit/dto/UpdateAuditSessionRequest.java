package com.govos.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateAuditSessionRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String sessionId,
        @NotNull
        Instant loginTime,
        Instant logoutTime,
        @Size(max = 45)
        String ipAddress,
        @Size(max = 255)
        String device,
        @Size(max = 255)
        String browser,
        Boolean active,
        Long version
) {
}
