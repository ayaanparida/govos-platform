package com.govos.doc.dto.document;

import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;

import java.util.UUID;

public record UpdateDocumentRequest(
        String title,
        String description,
        DocumentStatus status,
        DocumentClassification classification,
        UUID folderId,
        UUID categoryId,
        UUID retentionPolicyId,
        String moduleCode,
        String entityType,
        UUID referenceId,
        String documentNumber,
        String tags,
        String mimeType,
        Boolean active,
        Long version
) {
}
