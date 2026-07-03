package com.govos.doc.dto;

import com.govos.doc.entity.DocumentAccessAction;

import java.time.Instant;
import java.util.UUID;

public record DocumentAccessLogDto(
        UUID id,
        String code,
        UUID documentId,
        UUID userId,
        DocumentAccessAction action,
        Instant accessedAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
