package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintAssignmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ComplaintAssignmentCreateRequest(
        @NotNull
        UUID complaintId,
        @NotNull
        ComplaintAssignmentType assignmentType,
        UUID departmentId,
        UUID officeId,
        UUID officerUserId,
        UUID assignedByUserId,
        String remarks,
        Boolean active
) {
}
