package com.govos.api.cmp.request;

import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Escalate a complaint")
public record CreateEscalationRequest(
        @NotNull
        @Schema(description = "Escalation level", example = "L2")
        ComplaintEscalationLevel escalationLevel,
        @NotNull
        @Schema(description = "Escalation reason", example = "SLA_BREACH")
        ComplaintEscalationReason escalationReason,
        @Schema(description = "Target user for escalation")
        UUID escalatedToUserId,
        @Schema(description = "Target department for escalation")
        UUID escalatedToDepartmentId,
        @Schema(description = "Optional escalation remarks")
        String remarks,
        @NotNull
        @Schema(description = "When the escalation occurred")
        Instant escalatedAt,
        @Schema(description = "Active flag override")
        Boolean active
) {
}
