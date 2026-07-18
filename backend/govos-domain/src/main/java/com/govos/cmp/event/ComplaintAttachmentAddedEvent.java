package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintAttachmentType;
import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when an attachment is linked to a complaint (CMP-008).
 */
public record ComplaintAttachmentAddedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt,
        UUID attachmentId,
        UUID documentId,
        ComplaintAttachmentType attachmentType
) {
}
