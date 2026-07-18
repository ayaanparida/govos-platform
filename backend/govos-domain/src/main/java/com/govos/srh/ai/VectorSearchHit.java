package com.govos.srh.ai;

import java.util.Map;
import java.util.UUID;

public record VectorSearchHit(
        String id,
        UUID referenceId,
        UUID organizationId,
        String entityType,
        double similarityScore,
        Map<String, Object> metadata
) {
}
