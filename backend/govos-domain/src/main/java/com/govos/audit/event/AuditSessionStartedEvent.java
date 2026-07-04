package com.govos.audit.event;

import java.time.Instant;
import java.util.UUID;

public record AuditSessionStartedEvent(
        UUID auditSessionId,
        String sessionId,
        Instant loginTime,
        String ipAddress,
        Instant occurredAt
) {
}
