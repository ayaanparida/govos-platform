package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintCommentType;
import com.govos.cmp.enums.ComplaintVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ComplaintCommentCreateRequest(
        @NotNull
        UUID complaintId,
        @NotNull
        UUID authorUserId,
        @NotBlank
        String commentText,
        ComplaintVisibility visibility,
        ComplaintCommentType commentType,
        Boolean active
) {
}
