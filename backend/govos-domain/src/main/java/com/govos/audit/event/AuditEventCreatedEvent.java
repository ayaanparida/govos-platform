package com.govos.audit.event;

import com.govos.audit.entity.AuditAction;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.entity.AuditEventType;

import java.time.Instant;
import java.util.UUID;

public record AuditEventCreatedEvent(
        UUID auditEventId,
        String eventCode,
        AuditEventType eventType,
        String entityType,
        UUID entityId,
        AuditAction action,
        AuditEventStatus status,
        Instant occurredAt
) {
}
