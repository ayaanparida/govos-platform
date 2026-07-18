package com.govos.doc.dto.audit;

import com.govos.doc.enums.AccessOperation;

import java.time.Instant;
import java.util.UUID;

public record DocumentAccessLogResponse(
        UUID id,
        String code,
        UUID documentId,
        UUID userId,
        AccessOperation operation,
        Instant accessedAt,
        Boolean success,
        String ipAddress,
        String userAgent,
        String details,
        Boolean active
) {
}
