package com.govos.srh.engine;

import java.util.List;

public interface SearchEngineProvider {

    void createIndex(String physicalIndexName);

    void deleteIndex(String physicalIndexName);

    void indexDocument(String indexName, String documentId, String documentJson);

    void updateDocument(String indexName, String documentId, String documentJson);

    void deleteDocument(String indexName, String documentId);

    SearchEngineQueryResult search(SearchEngineQuery query);

    EngineAdvancedSearchResult advancedSearch(EngineAdvancedSearchRequest request);

    List<String> autocomplete(EngineAutocompleteRequest request);

    long countDocuments(EngineCountRequest request);

    List<String> suggest(EngineSuggestRequest request);

    BulkOperationResult bulkIndex(String indexName, List<EngineDocumentRequest> documents);

    BulkOperationResult bulkDelete(String indexName, List<String> documentIds);

    void switchAlias(String aliasName, String newPhysicalIndexName, String oldPhysicalIndexName);

    boolean indexExists(String physicalIndexName);

    SearchEngineHealthStatus health();
}
