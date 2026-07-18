package com.govos.srh.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.ai.SemanticSearchService;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.engine.SearchEngineHealthStatus;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.entity.SearchDocument;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchAdministrationServiceTest {

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

    private SearchIndex index;
    private SearchAlias activeAlias;

    @BeforeEach
    void setUp() {
        SearchQueryAnalytics analytics = new SearchQueryAnalytics(new ObjectMapper());
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
                analytics,
                semanticSearchService,
                searchSchedulerService);

        index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        activeAlias = SrhTestFixtures.searchAlias(SrhTestFixtures.ALIAS_ID, index);
        activeAlias.setActiveAlias(true);
    }

    @Test
    void shouldReturnClusterHealth() {
        SearchHealthDto health = new SearchHealthDto(
                "UP", 2, 4, 4, 1024L, null, 512L, null, 12.5, Instant.now());

        when(searchIndexService.health()).thenReturn(SearchEngineHealthStatus.UP);
        when(searchClusterMonitor.getDetailedHealth("UP")).thenReturn(health);

        assertThat(administrationService.getClusterHealth()).isEqualTo(health);
    }

    @Test
    void shouldReturnClusterInformation() {
        SearchClusterInfoDto cluster = new SearchClusterInfoDto(
                "govos-search", "green", 2, 4, 8, 0, 0, 0);

        when(searchClusterMonitor.getClusterInformation()).thenReturn(cluster);

        assertThat(administrationService.getClusterInformation()).isEqualTo(cluster);
    }

    @Test
    void shouldReturnNodeInformation() {
        List<SearchNodeInfoDto> nodes = List.of(
                new SearchNodeInfoDto("node-1", "node-1", "127.0.0.1", "online", 100L, 200L, 5.0));

        when(searchClusterMonitor.getNodeInformation()).thenReturn(nodes);

        assertThat(administrationService.getNodeInformation()).hasSize(1);
    }

    @Test
    void shouldReindexIndexUsingActiveAlias() {
        UUID jobId = UUID.randomUUID();
        SearchSyncJobDto createdJob = sampleJob(jobId, SearchJobStatus.PENDING);
        SearchSyncJobDto runningJob = sampleJob(jobId, SearchJobStatus.RUNNING);
        SearchSyncJobDto completedJob = sampleJob(jobId, SearchJobStatus.COMPLETED);

        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(index));
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(activeAlias));
        when(searchSyncJobService.create(any())).thenReturn(createdJob);
        when(searchSyncJobService.start(jobId)).thenReturn(runningJob);
        when(searchSyncJobService.complete(jobId)).thenReturn(completedJob);

        SearchSyncJobDto result = administrationService.reindexIndex(SrhTestFixtures.INDEX_ID);

        assertThat(result.jobStatus()).isEqualTo(SearchJobStatus.COMPLETED);
        verify(searchIndexService).switchAlias(SrhTestFixtures.INDEX_ID, SrhTestFixtures.ALIAS_ID);
    }

    @Test
    void shouldFailReindexWhenNoActiveAliasExists() {
        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(index));
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> administrationService.reindexIndex(SrhTestFixtures.INDEX_ID))
                .isInstanceOf(SearchAdministrationException.class)
                .hasMessageContaining("No active alias");
    }

    @Test
    void shouldCancelReindexJob() {
        UUID jobId = UUID.randomUUID();
        SearchSyncJobDto cancelled = sampleJob(jobId, SearchJobStatus.CANCELLED);

        when(searchSyncJobService.cancel(jobId)).thenReturn(cancelled);

        assertThat(administrationService.cancelReindex(jobId).jobStatus())
                .isEqualTo(SearchJobStatus.CANCELLED);
    }

    @Test
    void shouldReturnRunningJobs() {
        SearchSyncJob running = SrhTestFixtures.searchSyncJob(SrhTestFixtures.JOB_ID, index);
        running.setStatus(SearchJobStatus.RUNNING);

        when(searchSyncJobRepository.findAllByJobStatusAndDeletedFalse(SearchJobStatus.RUNNING))
                .thenReturn(List.of(running));
        when(searchSyncJobMapper.toDto(running)).thenReturn(sampleJob(SrhTestFixtures.JOB_ID, SearchJobStatus.RUNNING));

        assertThat(administrationService.getRunningJobs()).hasSize(1);
    }

    @Test
    void shouldReturnIndexStatistics() {
        SearchDocument document = SrhTestFixtures.searchDocument(SrhTestFixtures.DOCUMENT_ID, index);
        document.setLastIndexedAt(Instant.parse("2026-01-02T01:00:00Z"));

        when(searchIndexRepository.findByIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(Optional.of(index));
        when(searchDocumentRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(document));
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(activeAlias));
        when(searchIndexMonitor.getIndexStats(index.getPhysicalIndexName()))
                .thenReturn(new SearchIndexEngineStats(10L, 1L, 4096L));
        when(searchQueryHistoryRepository.findAllByCreatedDateBetweenAndDeletedFalse(any(), any()))
                .thenReturn(List.of(sampleHistory("water leak", 100L)));

        SearchIndexStatisticsDto stats = administrationService.getIndexStatistics(SrhTestFixtures.INDEX_ID);

        assertThat(stats.indexCode()).isEqualTo(SrhTestFixtures.INDEX_CODE);
        assertThat(stats.documentCount()).isEqualTo(10L);
        assertThat(stats.deletedCount()).isEqualTo(1L);
        assertThat(stats.storageSizeBytes()).isEqualTo(4096L);
        assertThat(stats.activeAlias()).isEqualTo(SrhTestFixtures.ALIAS_NAME);
    }

    private SearchSyncJobDto sampleJob(UUID jobId, SearchJobStatus status) {
        return new SearchSyncJobDto(
                jobId,
                "JOB-001",
                SrhTestFixtures.INDEX_ID,
                "Full reindex",
                SearchJobType.FULL_REINDEX,
                status,
                Instant.now(),
                status == SearchJobStatus.COMPLETED || status == SearchJobStatus.CANCELLED
                        ? Instant.now()
                        : null,
                0L,
                0L,
                0L,
                null,
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }

    private SearchQueryHistory sampleHistory(String queryText, long executionTimeMs) {
        SearchQueryHistory history = new SearchQueryHistory();
        history.setOrganizationId(SrhTestFixtures.ORG_ID);
        history.setQueryText(queryText);
        history.setQueryType(SearchQueryType.SEARCH);
        history.setFiltersJson("{\"entityType\":\"COMPLAINT\"}");
        history.setExecutionTimeMs(executionTimeMs);
        history.setSearchedAt(Instant.now());
        history.setCreatedDate(Instant.now());
        history.setDeleted(false);
        return history;
    }
}
