package com.govos.doc.dto.category;

import java.util.UUID;

public record UpdateDocumentCategoryRequest(
        String name,
        UUID parentCategoryId,
        UUID defaultRetentionPolicyId,
        String allowedMimeTypes,
        String description,
        Boolean active,
        Long version
) {
}
