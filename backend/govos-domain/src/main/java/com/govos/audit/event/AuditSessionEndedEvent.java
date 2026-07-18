package com.govos.audit.event;

import java.time.Instant;
import java.util.UUID;

public record AuditSessionEndedEvent(
        UUID auditSessionId,
        String sessionId,
        Instant loginTime,
        Instant logoutTime,
        Instant occurredAt
) {
}
