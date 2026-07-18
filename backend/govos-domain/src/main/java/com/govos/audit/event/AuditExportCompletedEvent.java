package com.govos.audit.event;

import com.govos.audit.entity.AuditExportType;

import java.time.Instant;
import java.util.UUID;

public record AuditExportCompletedEvent(
        UUID auditExportId,
        AuditExportType exportType,
        UUID requestedById,
        String fileName,
        Instant occurredAt
) {
}
