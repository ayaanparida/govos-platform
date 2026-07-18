package com.govos.srh.production;

import com.govos.srh.admin.SearchClusterMonitor;
import com.govos.srh.config.SearchProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SearchCacheWarmer {

    private final SearchCacheProperties cacheProperties;
    private final SearchClusterMonitor searchClusterMonitor;
    private final SearchReadCache searchReadCache;

    public SearchCacheWarmer(
            SearchProperties searchProperties,
            SearchClusterMonitor searchClusterMonitor,
            SearchReadCache searchReadCache) {
        this.cacheProperties = searchProperties.getCache();
        this.searchClusterMonitor = searchClusterMonitor;
        this.searchReadCache = searchReadCache;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmCaches() {
        if (!cacheProperties.isWarmOnStartup() || !cacheProperties.isEnabled()) {
            return;
        }
        searchReadCache.put("srh:cluster:info", searchClusterMonitor.getClusterInformation());
    }
}
