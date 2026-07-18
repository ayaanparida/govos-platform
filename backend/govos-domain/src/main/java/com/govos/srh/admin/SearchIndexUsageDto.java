package com.govos.srh.admin;

import java.time.Instant;
import java.util.UUID;

public record SearchIndexUsageDto(
        UUID indexId,
        String indexCode,
        long documentCount,
        Instant lastReindexedAt
) {
}
