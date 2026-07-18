package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintAttachmentType;

import java.time.Instant;
import java.util.UUID;

public record ComplaintAttachmentDto(
        UUID id,
        String code,
        UUID complaintId,
        UUID documentId,
        UUID documentVersionId,
        ComplaintAttachmentType attachmentType,
        String displayName,
        UUID uploadedByUserId,
        Integer sortOrder,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
