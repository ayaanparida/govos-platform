package com.govos.doc.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDocumentTagMappingRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID documentId,
        @NotNull
        UUID tagId,
        Boolean active
) {
}
