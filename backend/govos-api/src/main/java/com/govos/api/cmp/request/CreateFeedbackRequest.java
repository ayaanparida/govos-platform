package com.govos.api.cmp.request;

import com.govos.cmp.enums.ComplaintFeedbackRating;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Submit citizen feedback for a closed complaint")
public record CreateFeedbackRequest(
        @NotNull
        @Schema(description = "Feedback rating", example = "FOUR")
        ComplaintFeedbackRating rating,
        @Schema(description = "Optional feedback text")
        String feedback,
        @NotNull
        @Schema(description = "When the rating was submitted")
        Instant ratedAt,
        @Schema(description = "Active flag override")
        Boolean active
) {
}
