package com.govos.audit.dto;

import com.govos.audit.entity.AuditExportStatus;
import com.govos.audit.entity.AuditExportType;

import java.time.Instant;
import java.util.UUID;

public record AuditExportDto(
        UUID id,
        String code,
        AuditExportType exportType,
        UUID requestedById,
        Instant requestedTime,
        AuditExportStatus status,
        String fileName,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
