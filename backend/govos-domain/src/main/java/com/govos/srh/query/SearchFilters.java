package com.govos.srh.query;

import java.time.Instant;

public record SearchFilters(
        String entityType,
        String status,
        String priority,
        String category,
        String subCategory,
        Instant createdDateFrom,
        Instant createdDateTo,
        Instant updatedDateFrom,
        Instant updatedDateTo,
        Boolean active,
        Boolean deleted
) {
}
