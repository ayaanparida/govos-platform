package com.govos.srh.production;

import com.govos.srh.admin.SearchClusterInfoDto;
import com.govos.srh.admin.SearchClusterMonitor;
import com.govos.srh.admin.SearchHealthDto;
import com.govos.srh.admin.SearchNodeInfoDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class CachedSearchClusterMonitor implements SearchClusterMonitor {

    private final SearchClusterMonitor delegate;
    private final SearchReadCache searchReadCache;

    public CachedSearchClusterMonitor(
            @Qualifier("openSearchClusterMonitorDelegate") SearchClusterMonitor delegate,
            SearchReadCache searchReadCache) {
        this.delegate = delegate;
        this.searchReadCache = searchReadCache;
    }

    @Override
    public SearchClusterInfoDto getClusterInformation() {
        return searchReadCache.getOrLoad("srh:cluster:info", delegate::getClusterInformation);
    }

    @Override
    public List<SearchNodeInfoDto> getNodeInformation() {
        return searchReadCache.getOrLoad("srh:cluster:nodes", delegate::getNodeInformation);
    }

    @Override
    public SearchHealthDto getDetailedHealth(String engineStatus) {
        return searchReadCache.getOrLoad(
                "srh:cluster:health:" + engineStatus,
                () -> delegate.getDetailedHealth(engineStatus));
    }
}
