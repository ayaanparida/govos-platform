package com.govos.audit.dto;

import com.govos.audit.entity.AuditAction;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.entity.AuditEventType;

import java.time.Instant;
import java.util.UUID;

public record AuditEventDto(
        UUID id,
        String code,
        String eventCode,
        AuditEventType eventType,
        String entityType,
        UUID entityId,
        AuditAction action,
        String description,
        UUID actorId,
        UUID sessionId,
        String ipAddress,
        String userAgent,
        Instant eventTimestamp,
        AuditEventStatus status,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
