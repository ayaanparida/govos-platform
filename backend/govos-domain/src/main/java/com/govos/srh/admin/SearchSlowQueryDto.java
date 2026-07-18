package com.govos.srh.admin;

import java.time.Instant;
import java.util.UUID;

public record SearchSlowQueryDto(
        String queryText,
        UUID organizationId,
        long executionTimeMs,
        Instant searchedAt
) {
}
