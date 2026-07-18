package com.govos.doc.dto.document;

import com.govos.doc.enums.DocumentClassification;

import java.util.UUID;

public record CreateDocumentRequest(
        String title,
        String description,
        UUID organizationId,
        UUID ownerId,
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
        Boolean active
) {
}
