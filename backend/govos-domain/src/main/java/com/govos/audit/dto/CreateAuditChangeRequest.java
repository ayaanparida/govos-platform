package com.govos.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAuditChangeRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID auditEventId,
        @NotBlank @Size(max = 255)
        String fieldName,
        String oldValue,
        String newValue,
        Boolean active
) {
}
