package com.govos.srh.production;

import com.govos.srh.admin.SearchClusterMonitor;
import com.govos.srh.admin.SearchHealthDto;
import com.govos.srh.ai.SemanticSearchInfo;
import com.govos.srh.ai.SemanticSearchService;
import com.govos.srh.engine.SearchEngineHealthStatus;
import com.govos.srh.service.SearchIndexService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SearchOperationalHealthService {

    private final SearchIndexService searchIndexService;
    private final SearchClusterMonitor searchClusterMonitor;
    private final SemanticSearchService semanticSearchService;
    private final SearchReadCache searchReadCache;

    public SearchOperationalHealthService(
            SearchIndexService searchIndexService,
            SearchClusterMonitor searchClusterMonitor,
            SemanticSearchService semanticSearchService,
            SearchReadCache searchReadCache) {
        this.searchIndexService = searchIndexService;
        this.searchClusterMonitor = searchClusterMonitor;
        this.semanticSearchService = semanticSearchService;
        this.searchReadCache = searchReadCache;
    }

    public SearchOperationalHealthDto getOperationalHealth() {
        String engineStatus = searchIndexService.health().name();
        SearchHealthDto health = searchClusterMonitor.getDetailedHealth(engineStatus);
        var cluster = searchClusterMonitor.getClusterInformation();
        SemanticSearchInfo semanticInfo = semanticSearchService.getSemanticInfo();

        return new SearchOperationalHealthDto(
                engineStatus,
                health.nodeCount(),
                health.diskUsedBytes(),
                health.memoryUsedBytes(),
                cluster.relocatingShards() + cluster.initializingShards(),
                cluster.unassignedShards(),
                engineStatus.equals(SearchEngineHealthStatus.UP.name())
                        || engineStatus.equals(SearchEngineHealthStatus.DEGRADED.name()),
                semanticInfo.embeddingProviderHealth().name(),
                semanticInfo.vectorIndexHealth().name(),
                semanticInfo.semanticEnabled(),
                searchReadCache.healthStatus(),
                searchReadCache.size(),
                Instant.now());
    }
}
