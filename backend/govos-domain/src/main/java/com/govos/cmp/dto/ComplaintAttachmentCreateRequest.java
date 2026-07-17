package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintAttachmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ComplaintAttachmentCreateRequest(
        @NotNull
        UUID complaintId,
        @NotNull
        UUID documentId,
        UUID documentVersionId,
        @NotNull
        ComplaintAttachmentType attachmentType,
        @Size(max = 255)
        String displayName,
        @NotNull
        UUID uploadedByUserId,
        Integer sortOrder,
        Boolean active
) {
}
