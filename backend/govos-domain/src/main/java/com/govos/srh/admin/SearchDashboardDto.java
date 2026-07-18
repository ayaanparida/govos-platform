package com.govos.srh.admin;

import com.govos.srh.dto.SearchSyncJobDto;

import java.time.Instant;
import java.util.List;

public record SearchDashboardDto(
        SearchHealthDto health,
        SearchQueryStatisticsDto queryStatistics,
        SearchStatisticsDto platformStatistics,
        List<SearchSyncJobDto> runningJobs,
        List<SearchSyncJobDto> recentFailures,
        List<SearchIndexUsageDto> indexUsage,
        SearchSemanticInfoDto semanticInfo,
        Instant generatedAt
) {
}
