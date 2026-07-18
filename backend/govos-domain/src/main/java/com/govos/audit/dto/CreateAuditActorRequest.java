package com.govos.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAuditActorRequest(
        @Size(max = 100)
        String code,
        UUID userId,
        @NotBlank @Size(max = 255)
        String displayName,
        @Size(max = 255)
        String organization,
        @Size(max = 255)
        String department,
        Boolean active
) {
}
