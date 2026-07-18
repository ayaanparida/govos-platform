package com.govos.doc.dto.category;

import java.util.UUID;

public record DocumentCategoryResponse(
        UUID id,
        String code,
        String name,
        UUID organizationId,
        UUID parentCategoryId,
        UUID defaultRetentionPolicyId,
        String allowedMimeTypes,
        String description,
        Boolean active,
        Long version
) {
}
