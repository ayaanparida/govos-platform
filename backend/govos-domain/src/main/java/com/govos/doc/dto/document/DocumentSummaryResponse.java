package com.govos.doc.dto.document;

import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentSummaryResponse(
        UUID id,
        String code,
        String title,
        DocumentStatus status,
        DocumentClassification classification,
        String mimeType,
        String documentNumber,
        UUID organizationId,
        Instant createdDate
) {
}
