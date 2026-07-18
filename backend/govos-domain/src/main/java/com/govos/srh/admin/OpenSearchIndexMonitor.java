package com.govos.srh.admin;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.stereotype.Service;

@Service
public class OpenSearchIndexMonitor implements SearchIndexMonitor {

    private final OpenSearchClient openSearchClient;

    public OpenSearchIndexMonitor(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @Override
    public SearchIndexEngineStats getIndexStats(String physicalIndexName) {
        if (physicalIndexName == null || physicalIndexName.isBlank()) {
            return new SearchIndexEngineStats(0L, 0L, null);
        }

        try {
            var response = openSearchClient.indices().stats(request -> request.index(physicalIndexName));
            if (response.indices() == null || !response.indices().containsKey(physicalIndexName)) {
                return new SearchIndexEngineStats(0L, 0L, null);
            }

            var stats = response.indices().get(physicalIndexName);
            long docs = 0L;
            long deleted = 0L;
            Long storage = null;

            if (stats.total() != null) {
                if (stats.total().docs() != null) {
                    docs = safeLong(stats.total().docs().count());
                    deleted = safeLong(stats.total().docs().deleted());
                }
                if (stats.total().store() != null) {
                    storage = stats.total().store().sizeInBytes();
                }
            }

            return new SearchIndexEngineStats(docs, deleted, storage);
        } catch (Exception ex) {
            throw new SearchAdministrationException(
                    "Failed to load OpenSearch index statistics for: " + physicalIndexName, ex);
        }
    }

    private static long safeLong(Number value) {
        return value != null ? value.longValue() : 0L;
    }
}
