package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a complaint draft is created (CMP-008).
 */
public record ComplaintCreatedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt
) {
}
