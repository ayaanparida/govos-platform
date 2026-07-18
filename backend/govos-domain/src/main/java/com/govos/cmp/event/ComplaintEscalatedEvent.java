package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;
import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a complaint is escalated (CMP-008).
 */
public record ComplaintEscalatedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt,
        UUID escalationId,
        ComplaintEscalationLevel level,
        ComplaintEscalationReason reason
) {
}
