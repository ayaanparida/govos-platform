package com.govos.srh.admin;

import java.time.Instant;
import java.util.UUID;

public record SearchIndexStatisticsDto(
        UUID indexId,
        String indexCode,
        long documentCount,
        long deletedCount,
        Long storageSizeBytes,
        Instant lastIndexedAt,
        Instant lastReindexedAt,
        String activeAlias,
        Integer mappingVersion,
        long searchCount
) {
}
