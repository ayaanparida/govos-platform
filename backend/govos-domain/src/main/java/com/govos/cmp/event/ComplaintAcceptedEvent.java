package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a submitted complaint is accepted (CMP-008).
 */
public record ComplaintAcceptedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt,
        UUID acceptedByUserId
) {
}
