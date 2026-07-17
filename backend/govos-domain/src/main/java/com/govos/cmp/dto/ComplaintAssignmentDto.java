package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintAssignmentStatus;
import com.govos.cmp.enums.ComplaintAssignmentType;

import java.time.Instant;
import java.util.UUID;

public record ComplaintAssignmentDto(
        UUID id,
        String code,
        UUID complaintId,
        ComplaintAssignmentType assignmentType,
        UUID departmentId,
        UUID officeId,
        UUID officerUserId,
        UUID assignedByUserId,
        ComplaintAssignmentStatus assignmentStatus,
        Instant assignedAt,
        Instant acceptedAt,
        Instant rejectedAt,
        String rejectionReasonKey,
        String remarks,
        Boolean isCurrent,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
