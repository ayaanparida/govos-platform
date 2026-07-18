package com.govos.srh.production;

import com.govos.srh.config.SearchProperties;
import com.govos.srh.engine.BulkOperationResult;
import com.govos.srh.engine.EngineAdvancedSearchRequest;
import com.govos.srh.engine.EngineAdvancedSearchResult;
import com.govos.srh.engine.EngineAutocompleteRequest;
import com.govos.srh.engine.EngineCountRequest;
import com.govos.srh.engine.EngineDocumentRequest;
import com.govos.srh.engine.EngineSuggestRequest;
import com.govos.srh.engine.SearchEngineHealthStatus;
import com.govos.srh.engine.SearchEngineProvider;
import com.govos.srh.engine.SearchEngineQuery;
import com.govos.srh.engine.SearchEngineQueryResult;
import com.govos.srh.exception.SearchEngineException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Primary
public class ResilientSearchEngineProvider implements SearchEngineProvider {

    private final SearchEngineProvider delegate;
    private final OpenSearchRetryExecutor retryExecutor;
    private final SearchResilienceProperties resilienceProperties;
    private final SearchMetricsRecorder metricsRecorder;

    public ResilientSearchEngineProvider(
            @Qualifier("openSearchEngineProviderDelegate") SearchEngineProvider delegate,
            SearchProperties searchProperties,
            SearchMetricsRecorder metricsRecorder) {
        this.delegate = delegate;
        this.resilienceProperties = searchProperties.getResilience();
        this.retryExecutor = new OpenSearchRetryExecutor(resilienceProperties);
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public void createIndex(String physicalIndexName) {
        retryExecutor.executeVoid("createIndex", () -> {
            delegate.createIndex(physicalIndexName);
            metricsRecorder.recordIndexOperation("create");
        });
    }

    @Override
    public void deleteIndex(String physicalIndexName) {
        retryExecutor.executeVoid("deleteIndex", () -> {
            delegate.deleteIndex(physicalIndexName);
            metricsRecorder.recordIndexOperation("delete");
        });
    }

    @Override
    public void indexDocument(String indexName, String documentId, String documentJson) {
        retryExecutor.executeVoid("indexDocument", () -> delegate.indexDocument(indexName, documentId, documentJson));
    }

    @Override
    public void updateDocument(String indexName, String documentId, String documentJson) {
        retryExecutor.executeVoid("updateDocument", () -> delegate.updateDocument(indexName, documentId, documentJson));
    }

    @Override
    public void deleteDocument(String indexName, String documentId) {
        retryExecutor.executeVoid("deleteDocument", () -> delegate.deleteDocument(indexName, documentId));
    }

    @Override
    public SearchEngineQueryResult search(SearchEngineQuery query) {
        return executeRead("search", () -> delegate.search(query), emptyQueryResult());
    }

    @Override
    public EngineAdvancedSearchResult advancedSearch(EngineAdvancedSearchRequest request) {
        return executeRead("advancedSearch", () -> delegate.advancedSearch(request),
                new EngineAdvancedSearchResult(0L, List.of(), List.of()));
    }

    @Override
    public List<String> autocomplete(EngineAutocompleteRequest request) {
        return executeRead("autocomplete", () -> delegate.autocomplete(request), List.of());
    }

    @Override
    public long countDocuments(EngineCountRequest request) {
        return executeRead("countDocuments", () -> delegate.countDocuments(request), 0L);
    }

    @Override
    public List<String> suggest(EngineSuggestRequest request) {
        return executeRead("suggest", () -> delegate.suggest(request), List.of());
    }

    @Override
    public BulkOperationResult bulkIndex(String indexName, List<EngineDocumentRequest> documents) {
        BulkOperationResult result = retryExecutor.execute(
                "bulkIndex",
                () -> delegate.bulkIndex(indexName, documents));
        metricsRecorder.recordBulkOperation(result.successCount(), result.failureCount());
        return result;
    }

    @Override
    public BulkOperationResult bulkDelete(String indexName, List<String> documentIds) {
        BulkOperationResult result = retryExecutor.execute(
                "bulkDelete",
                () -> delegate.bulkDelete(indexName, documentIds));
        metricsRecorder.recordBulkOperation(result.successCount(), result.failureCount());
        return result;
    }

    @Override
    public void switchAlias(String aliasName, String newPhysicalIndexName, String oldPhysicalIndexName) {
        retryExecutor.executeVoid("switchAlias", () -> {
            delegate.switchAlias(aliasName, newPhysicalIndexName, oldPhysicalIndexName);
            metricsRecorder.recordAliasSwitch();
        });
    }

    @Override
    public boolean indexExists(String physicalIndexName) {
        return retryExecutor.execute("indexExists", () -> delegate.indexExists(physicalIndexName));
    }

    @Override
    public SearchEngineHealthStatus health() {
        try {
            SearchEngineHealthStatus status = delegate.health();
            metricsRecorder.recordClusterHealth(status.name());
            return status;
        } catch (Exception ex) {
            metricsRecorder.recordClusterHealth(SearchEngineHealthStatus.DOWN.name());
            return SearchEngineHealthStatus.DOWN;
        }
    }

    private <T> T executeRead(String operation, java.util.function.Supplier<T> action, T fallback) {
        try {
            return retryExecutor.execute(operation, action);
        } catch (SearchEngineException ex) {
            metricsRecorder.recordSearchError(operation);
            if (resilienceProperties.isGracefulDegradation()) {
                return fallback;
            }
            throw ex;
        }
    }

    private static SearchEngineQueryResult emptyQueryResult() {
        return new SearchEngineQueryResult(0L, Collections.emptyList());
    }
}
