package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintFeedbackRating;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ComplaintFeedbackCreateRequest(
        @NotNull
        UUID complaintId,
        @NotNull
        UUID ratedByUserId,
        @NotNull
        ComplaintFeedbackRating rating,
        String feedback,
        @NotNull
        Instant ratedAt,
        Boolean active
) {
}
