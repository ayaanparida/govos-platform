package com.govos.doc.dto;

import java.time.Instant;
import java.util.UUID;

public record FolderDto(
        UUID id,
        String code,
        String name,
        UUID parentFolderId,
        UUID ownerId,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
