package com.govos.srh.service;

import com.govos.srh.dto.IndexSearchDocumentRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Placeholder SRH implementation until the Search bounded context is fully delivered.
 * CMP application integrations call this service synchronously within the same transaction.
 */
@Service
public class SearchIndexServiceImpl implements SearchIndexService {

    @Override
    public void index(IndexSearchDocumentRequest request) {
        // No-op until OpenSearch-backed indexing is implemented in SRH.
    }

    @Override
    public void reindex(IndexSearchDocumentRequest request) {
        // No-op until OpenSearch-backed indexing is implemented in SRH.
    }

    @Override
    public void remove(String indexCode, UUID documentId) {
        // No-op until OpenSearch-backed indexing is implemented in SRH.
    }
}
