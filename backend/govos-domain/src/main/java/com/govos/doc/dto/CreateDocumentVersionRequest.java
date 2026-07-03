package com.govos.doc.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDocumentVersionRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID documentId,
        @NotNull
        Integer versionNumber,
        @Size(max = 128)
        String checksum,
        Long size,
        Boolean active
) {
}
