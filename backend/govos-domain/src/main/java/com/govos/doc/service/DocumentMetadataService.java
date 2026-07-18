package com.govos.doc.service;

import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.entity.DocumentMetadata;

import java.util.UUID;

public interface DocumentMetadataService {

    DocumentMetadata createMetadata(UUID documentId, UUID documentVersionId, UpdateDocumentMetadataRequest request);

    DocumentMetadata updateMetadata(UUID id, UpdateDocumentMetadataRequest request);

    DocumentMetadata replaceMetadata(UUID id, UpdateDocumentMetadataRequest request);

    DocumentMetadata findMetadata(UUID documentId, UUID documentVersionId);
}
