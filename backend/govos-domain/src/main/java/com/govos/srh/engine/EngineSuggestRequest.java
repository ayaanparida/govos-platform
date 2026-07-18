package com.govos.srh.engine;

import java.util.UUID;

public record EngineSuggestRequest(
        String indexName,
        UUID organizationId,
        String prefix,
        String entityType,
        int limit,
        long timeoutMs
) {
}
