package com.govos.api.cmp.request;

import com.govos.cmp.enums.ComplaintAssignmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Assign or reassign a complaint to an officer")
public record AssignComplaintRequest(
        @NotNull
        @Schema(description = "Assignment type", example = "INITIAL")
        ComplaintAssignmentType assignmentType,
        @Schema(description = "Target department identifier")
        UUID departmentId,
        @Schema(description = "Target office identifier")
        UUID officeId,
        @NotNull
        @Schema(description = "Officer user identifier")
        UUID officerUserId,
        @Schema(description = "Optional assignment remarks")
        String remarks,
        @Schema(description = "Active flag override")
        Boolean active
) {
}
