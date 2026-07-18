package com.govos.srh.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.ai.EmbeddingHealthStatus;
import com.govos.srh.ai.SemanticSearchInfo;
import com.govos.srh.ai.SemanticSearchService;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.engine.SearchEngineHealthStatus;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.entity.SearchSyncJob;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;
import com.govos.srh.enums.SearchQueryType;
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
class SearchDashboardTest {

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
    void shouldAggregateDashboardMetrics() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        index.setActiveDocumentCount(25L);
        index.setLastReindexedAt(Instant.parse("2026-01-05T00:00:00Z"));

        SearchSyncJob runningJob = SrhTestFixtures.searchSyncJob(SrhTestFixtures.JOB_ID, index);
        runningJob.setStatus(SearchJobStatus.RUNNING);

        SearchSyncJob failedJob = SrhTestFixtures.searchSyncJob(UUID.randomUUID(), index);
        failedJob.setStatus(SearchJobStatus.FAILED);
        failedJob.setCompletedAt(Instant.parse("2026-01-06T12:00:00Z"));

        SearchHealthDto health = new SearchHealthDto(
                "UP", 1, 2, 2, 100L, null, 50L, null, 10.0, Instant.now());

        when(searchIndexService.health()).thenReturn(SearchEngineHealthStatus.UP);
        when(searchClusterMonitor.getDetailedHealth("UP")).thenReturn(health);
        when(searchQueryHistoryRepository.findAllByCreatedDateBetweenAndDeletedFalse(any(), any()))
                .thenReturn(List.of(
                        history("water leak", 120L),
                        history("road repair", 80L)));
        when(searchIndexRepository.findAll()).thenReturn(List.of(index));
        when(searchSyncJobRepository.findAllByJobStatusAndDeletedFalse(SearchJobStatus.RUNNING))
                .thenReturn(List.of(runningJob));
        when(searchSyncJobRepository.findAll()).thenReturn(List.of(runningJob, failedJob));
        when(searchSyncJobMapper.toDto(runningJob)).thenReturn(jobDto(runningJob));
        when(searchSyncJobMapper.toDto(failedJob)).thenReturn(jobDto(failedJob));
        when(searchIndexRepository.findAllByActiveTrueAndDeletedFalse()).thenReturn(List.of(index));
        when(semanticSearchService.getSemanticInfo()).thenReturn(new SemanticSearchInfo(
                "mock", 384, false, EmbeddingHealthStatus.UP, EmbeddingHealthStatus.UP, 0L,
                "mock", 1, "UP", 0L));

        SearchDashboardDto dashboard = administrationService.getSearchDashboard();

        assertThat(dashboard.health()).isEqualTo(health);
        assertThat(dashboard.queryStatistics().totalQueries()).isEqualTo(2);
        assertThat(dashboard.platformStatistics().totalIndexes()).isEqualTo(1);
        assertThat(dashboard.runningJobs()).hasSize(1);
        assertThat(dashboard.recentFailures()).hasSize(1);
        assertThat(dashboard.indexUsage()).hasSize(1);
        assertThat(dashboard.indexUsage().getFirst().documentCount()).isEqualTo(25L);
        assertThat(dashboard.semanticInfo().provider()).isEqualTo("mock");
        assertThat(dashboard.generatedAt()).isNotNull();
    }

    private SearchQueryHistory history(String queryText, long executionTimeMs) {
        SearchQueryHistory history = new SearchQueryHistory();
        history.setOrganizationId(SrhTestFixtures.ORG_ID);
        history.setQueryText(queryText);
        history.setQueryType(SearchQueryType.SEARCH);
        history.setExecutionTimeMs(executionTimeMs);
        history.setSearchedAt(Instant.now());
        history.setCreatedDate(Instant.now());
        history.setDeleted(false);
        return history;
    }

    private SearchSyncJobDto jobDto(SearchSyncJob job) {
        return new SearchSyncJobDto(
                job.getId(),
                job.getCode(),
                job.getSearchIndex().getId(),
                job.getJobName(),
                job.getJobType(),
                job.getStatus(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getProcessedCount(),
                job.getSuccessCount(),
                job.getFailureCount(),
                job.getErrorMessage(),
                job.getActive(),
                job.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
