package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintFeedbackRating;
import jakarta.validation.constraints.NotNull;

public record ComplaintFeedbackUpdateRequest(
        ComplaintFeedbackRating rating,
        String feedback,
        Boolean active,
        @NotNull
        Long version
) {
}
