package com.govos.audit.dto;

import com.govos.audit.entity.AuditAction;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.entity.AuditEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateAuditEventRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 100)
        String eventCode,
        @NotNull
        AuditEventType eventType,
        @NotBlank @Size(max = 100)
        String entityType,
        @NotNull
        UUID entityId,
        @NotNull
        AuditAction action,
        @Size(max = 2000)
        String description,
        UUID actorId,
        UUID sessionId,
        @Size(max = 45)
        String ipAddress,
        @Size(max = 500)
        String userAgent,
        @NotNull
        Instant eventTimestamp,
        AuditEventStatus status,
        Boolean active
) {
}
