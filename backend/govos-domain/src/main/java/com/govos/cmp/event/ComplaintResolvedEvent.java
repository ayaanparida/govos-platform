package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a complaint is resolved (CMP-008).
 */
public record ComplaintResolvedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt,
        String resolutionReasonKey
) {
}
