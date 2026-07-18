package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when work begins on a complaint (CMP-008).
 */
public record ComplaintProgressStartedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt,
        UUID officerUserId
) {
}
