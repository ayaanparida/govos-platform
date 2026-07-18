package com.govos.doc.dto.document;

import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String code,
        String title,
        String description,
        UUID organizationId,
        UUID ownerId,
        DocumentStatus status,
        DocumentClassification classification,
        String mimeType,
        String moduleCode,
        String entityType,
        UUID referenceId,
        String documentNumber,
        String tags,
        UUID folderId,
        UUID categoryId,
        UUID retentionPolicyId,
        UUID activeVersionId,
        Boolean active,
        Long version
) {
}
