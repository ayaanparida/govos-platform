package com.govos.srh.scheduler;

import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchSchedulerServiceTest {

    @Test
    void shouldReturnSchedulerStatus() {
        SearchProperties properties = new SearchProperties();
        SearchSchedulerHistoryStore historyStore = new SearchSchedulerHistoryStore(properties);
        SearchSchedulerService service = new SearchSchedulerServiceImpl(
                properties,
                org.mockito.Mockito.mock(com.govos.srh.admin.SearchAdministrationService.class),
                org.mockito.Mockito.mock(com.govos.srh.production.SearchOperationalHealthService.class),
                org.mockito.Mockito.mock(com.govos.srh.service.SearchIndexService.class),
                org.mockito.Mockito.mock(com.govos.srh.admin.SearchClusterMonitor.class),
                org.mockito.Mockito.mock(com.govos.srh.admin.SearchIndexMonitor.class),
                org.mockito.Mockito.mock(com.govos.srh.production.SearchReadCache.class),
                org.mockito.Mockito.mock(com.govos.srh.ai.provider.EmbeddingCache.class),
                org.mockito.Mockito.mock(com.govos.srh.ai.job.EmbeddingGenerationService.class),
                org.mockito.Mockito.mock(com.govos.srh.ai.SemanticSearchService.class),
                org.mockito.Mockito.mock(com.govos.srh.repository.SearchDocumentRepository.class),
                org.mockito.Mockito.mock(com.govos.srh.repository.SearchIndexRepository.class),
                org.mockito.Mockito.mock(com.govos.srh.repository.SearchQueryHistoryRepository.class),
                historyStore,
                new SearchSchedulerRetryExecutor(new SearchSchedulerMetrics(
                        properties,
                        new com.govos.srh.production.SearchMetricsRecorder(
                                org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class),
                                properties))),
                new SearchSchedulerMetrics(
                        properties,
                        new com.govos.srh.production.SearchMetricsRecorder(
                                org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class),
                                properties)),
                new SearchSchedulerLogger());

        SearchSchedulerStatusDto status = service.getStatus();

        assertThat(status.enabled()).isTrue();
        assertThat(status.registeredJobs()).isNotEmpty();
    }
}
