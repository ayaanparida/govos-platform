package com.govos.srh.production;

import com.govos.srh.admin.SearchClusterInfoDto;
import com.govos.srh.admin.SearchClusterMonitor;
import com.govos.srh.admin.SearchHealthDto;
import com.govos.srh.ai.EmbeddingHealthStatus;
import com.govos.srh.ai.SemanticSearchInfo;
import com.govos.srh.ai.SemanticSearchService;
import com.govos.srh.engine.SearchEngineHealthStatus;
import com.govos.srh.service.SearchIndexService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchOperationalHealthServiceTest {

    @Mock
    private SearchIndexService searchIndexService;
    @Mock
    private SearchClusterMonitor searchClusterMonitor;
    @Mock
    private SemanticSearchService semanticSearchService;
    @Mock
    private SearchReadCache searchReadCache;

    @Test
    void shouldAggregateOperationalHealth() {
        when(searchIndexService.health()).thenReturn(SearchEngineHealthStatus.UP);
        when(searchClusterMonitor.getDetailedHealth("UP")).thenReturn(new SearchHealthDto(
                "UP", 2, 4, 4, 1024L, null, 512L, null, 10.0, Instant.now()));
        when(searchClusterMonitor.getClusterInformation()).thenReturn(new SearchClusterInfoDto(
                "govos", "green", 2, 4, 8, 1, 0, 1));
        when(semanticSearchService.getSemanticInfo()).thenReturn(new SemanticSearchInfo(
                "mock", 384, false, EmbeddingHealthStatus.UP, EmbeddingHealthStatus.UP, 5L,
                "mock", 1, "UP", 0L));
        when(searchReadCache.healthStatus()).thenReturn("UP");
        when(searchReadCache.size()).thenReturn(3L);

        SearchOperationalHealthService service = new SearchOperationalHealthService(
                searchIndexService, searchClusterMonitor, semanticSearchService, searchReadCache);

        SearchOperationalHealthDto health = service.getOperationalHealth();

        assertThat(health.clusterStatus()).isEqualTo("UP");
        assertThat(health.nodeCount()).isEqualTo(2);
        assertThat(health.unassignedShards()).isEqualTo(1);
        assertThat(health.semanticProviderHealth()).isEqualTo("UP");
        assertThat(health.cacheHealth()).isEqualTo("UP");
        assertThat(health.cacheEntries()).isEqualTo(3L);
    }
}
