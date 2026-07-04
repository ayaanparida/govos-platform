package com.govos.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateAuditEntityRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 100)
        String entityType,
        @NotNull
        UUID entityId,
        @Size(max = 500)
        String entityName,
        Boolean active,
        Long version
) {
}
