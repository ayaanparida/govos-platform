package com.govos.srh.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.exception.SearchEngineException;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.HealthStatus;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.UpdateAliasesRequest;
import org.opensearch.client.opensearch.indices.update_aliases.Action;
import org.opensearch.client.opensearch.indices.update_aliases.AddAction;
import org.opensearch.client.opensearch.indices.update_aliases.RemoveAction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("openSearchEngineProviderDelegate")
public class OpenSearchEngineProvider implements SearchEngineProvider {

    private final OpenSearchClient openSearchClient;
    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper;

    public OpenSearchEngineProvider(
            OpenSearchClient openSearchClient,
            SearchProperties searchProperties,
            ObjectMapper objectMapper) {
        this.openSearchClient = openSearchClient;
        this.searchProperties = searchProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void createIndex(String physicalIndexName) {
        try {
            CreateIndexRequest request = new CreateIndexRequest.Builder()
                    .index(physicalIndexName)
                    .settings(IndexSettings.of(settings -> settings
                            .numberOfShards("1")
                            .numberOfReplicas("0")))
                    .mappings(TypeMapping.of(mapping -> mapping.dynamic(org.opensearch.client.opensearch._types.mapping.DynamicMapping.True)
                            .properties("searchText", property -> property.text(text -> text))
                            .properties("organizationId", property -> property.keyword(keyword -> keyword))
                            .properties("entityType", property -> property.keyword(keyword -> keyword))
                            .properties("active", property -> property.boolean_(bool -> bool))
                            .properties("deleted", property -> property.boolean_(bool -> bool))))
                    .build();
            openSearchClient.indices().create(request);
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to create OpenSearch index: " + physicalIndexName, ex);
        }
    }

    @Override
    public void deleteIndex(String physicalIndexName) {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest.Builder()
                    .index(physicalIndexName)
                    .build();
            openSearchClient.indices().delete(request);
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to delete OpenSearch index: " + physicalIndexName, ex);
        }
    }

    @Override
    public void indexDocument(String indexName, String documentId, String documentJson) {
        try {
            openSearchClient.index(builder -> builder
                    .index(indexName)
                    .id(documentId)
                    .document(parseDocument(documentJson))
                    .refresh(Refresh.False));
        } catch (Exception ex) {
            throw new SearchEngineException(
                    "Failed to index document " + documentId + " into " + indexName, ex);
        }
    }

    @Override
    public void updateDocument(String indexName, String documentId, String documentJson) {
        indexDocument(indexName, documentId, documentJson);
    }

    @Override
    public void deleteDocument(String indexName, String documentId) {
        try {
            openSearchClient.delete(builder -> builder
                    .index(indexName)
                    .id(documentId)
                    .refresh(Refresh.False));
        } catch (Exception ex) {
            throw new SearchEngineException(
                    "Failed to delete document " + documentId + " from " + indexName, ex);
        }
    }

    @Override
    public SearchEngineQueryResult search(SearchEngineQuery query) {
        try {
            Query matchQuery = Query.of(q -> q.multiMatch(m -> m
                    .query(query.queryText())
                    .fields("searchText", "title", "description")));

            SearchRequest request = new SearchRequest.Builder()
                    .index(query.indexName())
                    .from(query.from())
                    .size(query.size())
                    .query(matchQuery)
                    .build();

            var response = openSearchClient.search(request, Map.class);
            List<Map<String, Object>> hits = new ArrayList<>();
            if (response.hits().hits() != null) {
                for (Hit<Map> hit : response.hits().hits()) {
                    Map<String, Object> hitMap = new HashMap<>();
                    hitMap.put("id", hit.id());
                    hitMap.put("score", hit.score());
                    hitMap.put("source", hit.source());
                    hits.add(hitMap);
                }
            }
            long total = response.hits().total() != null ? response.hits().total().value() : hits.size();
            return new SearchEngineQueryResult(total, hits);
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to execute OpenSearch query on " + query.indexName(), ex);
        }
    }

    @Override
    public EngineAdvancedSearchResult advancedSearch(EngineAdvancedSearchRequest request) {
        return OpenSearchQueryExecutor.advancedSearch(openSearchClient, request);
    }

    @Override
    public List<String> autocomplete(EngineAutocompleteRequest request) {
        return OpenSearchQueryExecutor.autocomplete(openSearchClient, request);
    }

    @Override
    public long countDocuments(EngineCountRequest request) {
        return OpenSearchQueryExecutor.count(openSearchClient, request);
    }

    @Override
    public List<String> suggest(EngineSuggestRequest request) {
        return OpenSearchQueryExecutor.suggest(openSearchClient, request);
    }

    @Override
    public BulkOperationResult bulkIndex(String indexName, List<EngineDocumentRequest> documents) {
        if (documents == null || documents.isEmpty()) {
            return new BulkOperationResult(0, 0);
        }

        long success = 0;
        long failure = 0;
        int batchSize = Math.max(1, searchProperties.getBulkBatchSize());

        for (int offset = 0; offset < documents.size(); offset += batchSize) {
            List<EngineDocumentRequest> batch = documents.subList(
                    offset, Math.min(offset + batchSize, documents.size()));
            BulkOperationResult batchResult = executeBulkIndexBatch(indexName, batch);
            success += batchResult.successCount();
            failure += batchResult.failureCount();
        }

        return new BulkOperationResult(success, failure);
    }

    @Override
    public BulkOperationResult bulkDelete(String indexName, List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return new BulkOperationResult(0, 0);
        }

        long success = 0;
        long failure = 0;
        int batchSize = Math.max(1, searchProperties.getBulkBatchSize());

        for (int offset = 0; offset < documentIds.size(); offset += batchSize) {
            List<String> batch = documentIds.subList(
                    offset, Math.min(offset + batchSize, documentIds.size()));
            BulkOperationResult batchResult = executeBulkDeleteBatch(indexName, batch);
            success += batchResult.successCount();
            failure += batchResult.failureCount();
        }

        return new BulkOperationResult(success, failure);
    }

    @Override
    public void switchAlias(String aliasName, String newPhysicalIndexName, String oldPhysicalIndexName) {
        try {
            List<Action> actions = new ArrayList<>();
            actions.add(Action.of(a -> a.add(AddAction.of(add -> add
                    .index(newPhysicalIndexName)
                    .alias(aliasName)))));
            if (oldPhysicalIndexName != null && !oldPhysicalIndexName.isBlank()) {
                actions.add(Action.of(a -> a.remove(RemoveAction.of(remove -> remove
                        .index(oldPhysicalIndexName)
                        .alias(aliasName)))));
            }

            UpdateAliasesRequest request = new UpdateAliasesRequest.Builder()
                    .actions(actions)
                    .build();
            openSearchClient.indices().updateAliases(request);
        } catch (Exception ex) {
            throw new SearchEngineException(
                    "Failed to switch alias " + aliasName + " to " + newPhysicalIndexName, ex);
        }
    }

    @Override
    public boolean indexExists(String physicalIndexName) {
        try {
            ExistsRequest request = new ExistsRequest.Builder()
                    .index(physicalIndexName)
                    .build();
            return openSearchClient.indices().exists(request).value();
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to check OpenSearch index existence: " + physicalIndexName, ex);
        }
    }

    @Override
    public SearchEngineHealthStatus health() {
        try {
            var response = openSearchClient.cluster().health();
            HealthStatus status = response.status();
            if (status == HealthStatus.Green) {
                return SearchEngineHealthStatus.UP;
            }
            if (status == HealthStatus.Yellow) {
                return SearchEngineHealthStatus.DEGRADED;
            }
            return SearchEngineHealthStatus.DOWN;
        } catch (Exception ex) {
            return SearchEngineHealthStatus.DOWN;
        }
    }

    private BulkOperationResult executeBulkIndexBatch(String indexName, List<EngineDocumentRequest> batch) {
        try {
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder().refresh(Refresh.False);
            for (EngineDocumentRequest document : batch) {
                bulkBuilder.operations(op -> op.index(idx -> idx
                        .index(indexName)
                        .id(document.documentId())
                        .document(parseDocument(document.documentJson()))));
            }

            var response = openSearchClient.bulk(bulkBuilder.build());
            long failures = response.errors()
                    ? response.items().stream().filter(item -> item.error() != null).count()
                    : 0;
            long successes = batch.size() - failures;
            return new BulkOperationResult(successes, failures);
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to bulk index documents into " + indexName, ex);
        }
    }

    private BulkOperationResult executeBulkDeleteBatch(String indexName, List<String> batch) {
        try {
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder().refresh(Refresh.False);
            for (String documentId : batch) {
                bulkBuilder.operations(op -> op.delete(del -> del
                        .index(indexName)
                        .id(documentId)));
            }

            var response = openSearchClient.bulk(bulkBuilder.build());
            long failures = response.errors()
                    ? response.items().stream().filter(item -> item.error() != null).count()
                    : 0;
            long successes = batch.size() - failures;
            return new BulkOperationResult(successes, failures);
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to bulk delete documents from " + indexName, ex);
        }
    }

    private Map<String, Object> parseDocument(String documentJson) {
        try {
            return objectMapper.readValue(documentJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            throw new SearchEngineException("Failed to parse document JSON for OpenSearch indexing", ex);
        }
    }
}
