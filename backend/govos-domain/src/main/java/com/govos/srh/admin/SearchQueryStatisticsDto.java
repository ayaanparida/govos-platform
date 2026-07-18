package com.govos.srh.admin;

import java.util.List;

public record SearchQueryStatisticsDto(
        long totalQueries,
        double averageResponseTimeMs,
        long queriesLast24Hours,
        long queriesLast7Days,
        List<SearchDailyVolumeDto> volumePerDay
) {
}
