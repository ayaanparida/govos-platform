package com.govos.srh.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.entity.SearchQueryHistory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class SearchQueryAnalytics {

    private static final int DEFAULT_TOP_LIMIT = 10;
    private static final int DEFAULT_SLOW_LIMIT = 10;
    private static final int ANALYTICS_WINDOW_DAYS = 30;

    private final ObjectMapper objectMapper;

    SearchQueryAnalytics(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    List<SearchQueryHistory> recentHistory(List<SearchQueryHistory> histories) {
        Instant cutoff = Instant.now().minus(ANALYTICS_WINDOW_DAYS, ChronoUnit.DAYS);
        return histories.stream()
                .filter(history -> history.getSearchedAt() != null && !history.getSearchedAt().isBefore(cutoff))
                .toList();
    }

    SearchQueryStatisticsDto buildQueryStatistics(List<SearchQueryHistory> histories) {
        List<SearchQueryHistory> recent = recentHistory(histories);
        Instant now = Instant.now();
        Instant last24Hours = now.minus(24, ChronoUnit.HOURS);
        Instant last7Days = now.minus(7, ChronoUnit.DAYS);

        double average = recent.stream()
                .mapToLong(SearchQueryHistory::getExecutionTimeMs)
                .average()
                .orElse(0D);

        long queries24h = recent.stream()
                .filter(history -> history.getSearchedAt() != null && !history.getSearchedAt().isBefore(last24Hours))
                .count();
        long queries7d = recent.stream()
                .filter(history -> history.getSearchedAt() != null && !history.getSearchedAt().isBefore(last7Days))
                .count();

        Map<LocalDate, Long> volumeByDay = recent.stream()
                .collect(Collectors.groupingBy(
                        history -> history.getSearchedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                        Collectors.counting()));

        List<SearchDailyVolumeDto> volumePerDay = volumeByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new SearchDailyVolumeDto(entry.getKey().toString(), entry.getValue()))
                .toList();

        return new SearchQueryStatisticsDto(
                recent.size(),
                average,
                queries24h,
                queries7d,
                volumePerDay);
    }

    List<SearchTopQueryDto> topQueries(List<SearchQueryHistory> histories, int limit) {
        Map<String, List<SearchQueryHistory>> grouped = recentHistory(histories).stream()
                .filter(history -> history.getQueryText() != null && !history.getQueryText().isBlank())
                .collect(Collectors.groupingBy(SearchQueryHistory::getQueryText));

        return grouped.entrySet().stream()
                .map(entry -> new SearchTopQueryDto(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .mapToLong(SearchQueryHistory::getExecutionTimeMs)
                                .average()
                                .orElse(0D)))
                .sorted(Comparator.comparingLong(SearchTopQueryDto::count).reversed())
                .limit(limit > 0 ? limit : DEFAULT_TOP_LIMIT)
                .toList();
    }

    List<SearchTopQueryDto> topOrganizations(List<SearchQueryHistory> histories, int limit) {
        Map<UUID, List<SearchQueryHistory>> grouped = recentHistory(histories).stream()
                .collect(Collectors.groupingBy(SearchQueryHistory::getOrganizationId));

        return grouped.entrySet().stream()
                .map(entry -> new SearchTopQueryDto(
                        entry.getKey().toString(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .mapToLong(SearchQueryHistory::getExecutionTimeMs)
                                .average()
                                .orElse(0D)))
                .sorted(Comparator.comparingLong(SearchTopQueryDto::count).reversed())
                .limit(limit > 0 ? limit : DEFAULT_TOP_LIMIT)
                .toList();
    }

    List<SearchTopQueryDto> topEntityTypes(List<SearchQueryHistory> histories, int limit) {
        Map<String, Long> counts = new HashMap<>();
        for (SearchQueryHistory history : recentHistory(histories)) {
            String entityType = extractEntityType(history.getFiltersJson());
            if (entityType != null) {
                counts.merge(entityType, 1L, Long::sum);
            }
        }

        return counts.entrySet().stream()
                .map(entry -> new SearchTopQueryDto(entry.getKey(), entry.getValue(), 0D))
                .sorted(Comparator.comparingLong(SearchTopQueryDto::count).reversed())
                .limit(limit > 0 ? limit : DEFAULT_TOP_LIMIT)
                .toList();
    }

    List<SearchTopQueryDto> topFilters(List<SearchQueryHistory> histories, int limit) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (SearchQueryHistory history : recentHistory(histories)) {
            if (history.getFiltersJson() != null && !history.getFiltersJson().isBlank()) {
                counts.merge(history.getFiltersJson(), 1L, Long::sum);
            }
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit > 0 ? limit : DEFAULT_TOP_LIMIT)
                .map(entry -> new SearchTopQueryDto(entry.getKey(), entry.getValue(), 0D))
                .toList();
    }

    List<SearchSlowQueryDto> slowQueries(List<SearchQueryHistory> histories, int limit) {
        return recentHistory(histories).stream()
                .sorted(Comparator.comparingLong(SearchQueryHistory::getExecutionTimeMs).reversed())
                .limit(limit > 0 ? limit : DEFAULT_SLOW_LIMIT)
                .map(history -> new SearchSlowQueryDto(
                        history.getQueryText(),
                        history.getOrganizationId(),
                        history.getExecutionTimeMs(),
                        history.getSearchedAt()))
                .toList();
    }

    private String extractEntityType(String filtersJson) {
        if (filtersJson == null || filtersJson.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(filtersJson);
            JsonNode entityType = node.get("entityType");
            return entityType != null && !entityType.isNull() ? entityType.asText() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
