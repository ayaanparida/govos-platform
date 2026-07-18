package com.govos.srh.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.enums.SearchQueryType;
import com.govos.srh.ai.SemanticSearchService;
import com.govos.srh.mapper.SearchSyncJobMapper;
import com.govos.srh.repository.SearchAliasRepository;
import com.govos.srh.repository.SearchDocumentRepository;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.repository.SearchQueryHistoryRepository;
import com.govos.srh.repository.SearchSyncJobRepository;
import com.govos.srh.service.SearchIndexService;
import com.govos.srh.service.SearchSyncJobService;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchStatisticsTest {

    @Mock
    private SearchIndexService searchIndexService;
    @Mock
    private SearchClusterMonitor searchClusterMonitor;
    @Mock
    private SearchIndexMonitor searchIndexMonitor;
    @Mock
    private SearchQueryHistoryRepository searchQueryHistoryRepository;
    @Mock
    private SearchSyncJobRepository searchSyncJobRepository;
    @Mock
    private SearchSyncJobService searchSyncJobService;
    @Mock
    private SearchSyncJobMapper searchSyncJobMapper;
    @Mock
    private SearchIndexRepository searchIndexRepository;
    @Mock
    private SearchDocumentRepository searchDocumentRepository;
    @Mock
    private SearchAliasRepository searchAliasRepository;
    @Mock
    private SemanticSearchService semanticSearchService;
    @Mock
    private com.govos.srh.scheduler.SearchSchedulerService searchSchedulerService;

    private SearchAdministrationService administrationService;

    @BeforeEach
    void setUp() {
        administrationService = new SearchAdministrationServiceImpl(
                searchIndexService,
                searchClusterMonitor,
                searchIndexMonitor,
                searchQueryHistoryRepository,
                searchSyncJobRepository,
                searchSyncJobService,
                searchSyncJobMapper,
                searchIndexRepository,
                searchDocumentRepository,
                searchAliasRepository,
                new SearchQueryAnalytics(new ObjectMapper()),
                semanticSearchService,
                searchSchedulerService);
    }

    @Test
    void shouldComputePlatformStatistics() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        index.setActiveDocumentCount(100L);

        when(searchIndexRepository.findAll()).thenReturn(List.of(index));
        when(searchQueryHistoryRepository.findAllByCreatedDateBetweenAndDeletedFalse(any(), any()))
                .thenReturn(List.of(
                        history("water leak", 100L),
                        history("water leak", 200L),
                        history("pothole", 300L)));
        when(searchSyncJobRepository.findAllByJobStatusAndDeletedFalse(any())).thenReturn(List.of());
        when(searchSyncJobRepository.findAll()).thenReturn(List.of());

        SearchStatisticsDto statistics = administrationService.getSearchStatistics();

        assertThat(statistics.totalIndexes()).isEqualTo(1);
        assertThat(statistics.activeIndexes()).isEqualTo(1);
        assertThat(statistics.totalDocuments()).isEqualTo(100L);
        assertThat(statistics.totalQueries()).isEqualTo(3);
        assertThat(statistics.averageQueryTimeMs()).isEqualTo(200D);
    }

    @Test
    void shouldComputeQueryAnalytics() {
        UUID orgTwo = UUID.fromString("44444444-4444-4444-4444-444444444444");

        when(searchQueryHistoryRepository.findAllByCreatedDateBetweenAndDeletedFalse(any(), any()))
                .thenReturn(List.of(
                        history("water leak", 500L, SrhTestFixtures.ORG_ID, "{\"entityType\":\"COMPLAINT\"}"),
                        history("water leak", 100L, SrhTestFixtures.ORG_ID, "{\"entityType\":\"COMPLAINT\"}"),
                        history("road repair", 50L, orgTwo, "{\"entityType\":\"WORK_ORDER\"}")));

        SearchQueryStatisticsDto queryStatistics = administrationService.getQueryStatistics();

        assertThat(queryStatistics.totalQueries()).isEqualTo(3);
        assertThat(queryStatistics.averageResponseTimeMs()).isEqualTo(216.666, org.assertj.core.data.Offset.offset(0.01));
        assertThat(queryStatistics.volumePerDay()).isNotEmpty();

        List<SearchTopQueryDto> topQueries = administrationService.getTopQueries(5);
        assertThat(topQueries.getFirst().queryText()).isEqualTo("water leak");
        assertThat(topQueries.getFirst().count()).isEqualTo(2);

        List<SearchTopQueryDto> topOrganizations = administrationService.getTopOrganizations(5);
        assertThat(topOrganizations.getFirst().queryText()).isEqualTo(SrhTestFixtures.ORG_ID.toString());
        assertThat(topOrganizations.getFirst().count()).isEqualTo(2);

        List<SearchTopQueryDto> topEntityTypes = administrationService.getTopEntityTypes(5);
        assertThat(topEntityTypes).extracting(SearchTopQueryDto::queryText)
                .contains("COMPLAINT", "WORK_ORDER");

        List<SearchSlowQueryDto> slowQueries = administrationService.getSlowQueries(2);
        assertThat(slowQueries.getFirst().queryText()).isEqualTo("water leak");
        assertThat(slowQueries.getFirst().executionTimeMs()).isEqualTo(500L);
    }

    private SearchQueryHistory history(String queryText, long executionTimeMs) {
        return history(queryText, executionTimeMs, SrhTestFixtures.ORG_ID, "{}");
    }

    private SearchQueryHistory history(String queryText, long executionTimeMs, UUID organizationId, String filters) {
        SearchQueryHistory history = new SearchQueryHistory();
        history.setOrganizationId(organizationId);
        history.setQueryText(queryText);
        history.setQueryType(SearchQueryType.SEARCH);
        history.setFiltersJson(filters);
        history.setExecutionTimeMs(executionTimeMs);
        history.setSearchedAt(Instant.now());
        history.setCreatedDate(Instant.now());
        history.setDeleted(false);
        return history;
    }
}
