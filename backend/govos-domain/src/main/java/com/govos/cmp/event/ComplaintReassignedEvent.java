package com.govos.cmp.event;

import com.govos.cmp.enums.ComplaintAssignmentType;
import com.govos.cmp.enums.ComplaintStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event contract emitted when a complaint is reassigned (CMP-008).
 */
public record ComplaintReassignedEvent(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID changedByUserId,
        ComplaintStatus status,
        Instant occurredAt,
        UUID officerUserId,
        UUID departmentId,
        UUID officeId,
        ComplaintAssignmentType assignmentType
) {
}
