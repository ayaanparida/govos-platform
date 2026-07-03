package com.govos.doc.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDocumentVersionRequest(
        @Size(max = 100)
        String code,
        @NotNull
        Integer versionNumber,
        @Size(max = 128)
        String checksum,
        Long size,
        Boolean active,
        Long version
) {
}
