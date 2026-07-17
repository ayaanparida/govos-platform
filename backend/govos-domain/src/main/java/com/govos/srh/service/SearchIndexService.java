package com.govos.srh.service;

import com.govos.srh.dto.IndexSearchDocumentRequest;

import java.util.UUID;

public interface SearchIndexService {

    void index(IndexSearchDocumentRequest request);

    void reindex(IndexSearchDocumentRequest request);

    void remove(String indexCode, UUID documentId);
}
