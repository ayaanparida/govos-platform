package com.govos.audit.dto;

import com.govos.audit.entity.AuditExportStatus;
import com.govos.audit.entity.AuditExportType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record UpdateAuditExportRequest(
        @Size(max = 100)
        String code,
        @NotNull
        AuditExportType exportType,
        @NotNull
        UUID requestedById,
        @NotNull
        Instant requestedTime,
        AuditExportStatus status,
        @Size(max = 500)
        String fileName,
        Boolean active,
        Long version
) {
}
