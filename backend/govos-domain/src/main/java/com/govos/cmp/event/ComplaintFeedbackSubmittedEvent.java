package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintFeedbackRating;
import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when citizen feedback is submitted (CMP-008).
 */
public record ComplaintFeedbackSubmittedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt,
        UUID feedbackId,
        ComplaintFeedbackRating rating
) {
}
