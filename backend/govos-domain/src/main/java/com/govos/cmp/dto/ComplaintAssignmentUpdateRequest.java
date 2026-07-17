package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintAssignmentStatus;
import com.govos.cmp.enums.ComplaintAssignmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ComplaintAssignmentUpdateRequest(
        ComplaintAssignmentType assignmentType,
        UUID departmentId,
        UUID officeId,
        UUID officerUserId,
        ComplaintAssignmentStatus assignmentStatus,
        @Size(max = 100)
        String rejectionReasonKey,
        String remarks,
        Boolean isCurrent,
        Boolean active,
        @NotNull
        Long version
) {
}
