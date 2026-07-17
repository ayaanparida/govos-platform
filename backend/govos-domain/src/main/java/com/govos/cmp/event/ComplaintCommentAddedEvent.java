package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.enums.ComplaintVisibility;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a comment is added to a complaint (CMP-008).
 */
public record ComplaintCommentAddedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt,
        UUID commentId,
        ComplaintVisibility visibility
) {
}
