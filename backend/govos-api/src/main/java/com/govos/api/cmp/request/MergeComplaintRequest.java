package com.govos.api.cmp.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Merge a complaint into a surviving complaint")
public record MergeComplaintRequest(
        @NotNull
        @Schema(description = "Surviving complaint identifier")
        UUID survivingComplaintId,
        @NotNull
        @Schema(description = "Complaint to merge into the survivor")
        UUID mergedComplaintId,
        @Schema(description = "Optional merge reason")
        String mergeReason,
        @NotNull
        @Schema(description = "When the merge occurred")
        Instant mergedAt,
        @Schema(description = "Active flag override")
        Boolean active
) {
}
