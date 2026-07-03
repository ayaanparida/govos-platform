package com.govos.doc.dto;

import com.govos.doc.entity.DocumentStatus;
import com.govos.doc.entity.DocumentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDocumentRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 500)
        String originalFilename,
        @NotBlank @Size(max = 500)
        String storedFilename,
        @Size(max = 255)
        String mimeType,
        @Size(max = 50)
        String extension,
        Long size,
        @Size(max = 128)
        String checksum,
        @NotNull
        UUID storageProviderId,
        UUID folderId,
        @NotNull
        UUID ownerId,
        DocumentVisibility visibility,
        DocumentStatus status,
        Boolean active
) {
}
