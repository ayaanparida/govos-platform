package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintCommentType;
import com.govos.cmp.enums.ComplaintVisibility;

import java.time.Instant;
import java.util.UUID;

public record ComplaintCommentDto(
        UUID id,
        String code,
        UUID complaintId,
        UUID authorUserId,
        String commentText,
        ComplaintVisibility visibility,
        ComplaintCommentType commentType,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
