package com.govos.srh.admin;

import java.time.Instant;

public record SearchStatisticsDto(
        long totalIndexes,
        long activeIndexes,
        long totalDocuments,
        long totalQueries,
        double averageQueryTimeMs,
        long runningJobs,
        long failedJobsLast24Hours,
        Instant generatedAt
) {
}
