package com.govos.doc.dto.document;

import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;

import java.util.UUID;

public record DocumentSearchResponse(
        UUID id,
        String code,
        String title,
        String description,
        String documentNumber,
        DocumentStatus status,
        DocumentClassification classification,
        String mimeType,
        UUID organizationId,
        UUID folderId,
        UUID categoryId
) {
}
