package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintFeedbackRating;

import java.time.Instant;
import java.util.UUID;

public record ComplaintFeedbackDto(
        UUID id,
        String code,
        UUID complaintId,
        UUID ratedByUserId,
        ComplaintFeedbackRating rating,
        String feedback,
        Instant ratedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
