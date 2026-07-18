package com.govos.srh.scheduler;

import java.util.List;

public record SearchSchedulerStatusDto(
        boolean enabled,
        String reindexCron,
        String incrementalReindexCron,
        String embeddingCron,
        String cleanupCron,
        String healthCron,
        String statisticsCron,
        int maxRetries,
        long totalExecutions,
        long failedExecutions,
        List<String> registeredJobs
) {
}
