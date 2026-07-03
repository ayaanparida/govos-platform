package com.govos.doc.dto;

import com.govos.doc.entity.DocumentStatus;
import com.govos.doc.entity.DocumentVisibility;

import java.time.Instant;
import java.util.UUID;

public record DocumentDto(
        UUID id,
        String code,
        String originalFilename,
        String storedFilename,
        String mimeType,
        String extension,
        Long size,
        String checksum,
        UUID storageProviderId,
        UUID folderId,
        UUID ownerId,
        DocumentVisibility visibility,
        DocumentStatus status,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
