package com.govos.doc.dto.category;

import java.util.UUID;

public record CreateDocumentCategoryRequest(
        String code,
        String name,
        UUID organizationId,
        UUID parentCategoryId,
        UUID defaultRetentionPolicyId,
        String allowedMimeTypes,
        String description,
        Boolean active
) {
}
