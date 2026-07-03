package com.govos.doc.dto;

import com.govos.doc.entity.DocumentAccessAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateDocumentAccessLogRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID documentId,
        @NotNull
        UUID userId,
        @NotNull
        DocumentAccessAction action,
        Instant accessedAt
) {
}
