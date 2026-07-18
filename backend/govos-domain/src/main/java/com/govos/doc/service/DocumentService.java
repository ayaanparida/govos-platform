package com.govos.doc.service;

import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.enums.DocumentClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DocumentService {

    Document createDocument(CreateDocumentRequest request);

    Document updateDocument(UUID id, UpdateDocumentRequest request);

    void deleteDocument(UUID id);

    Document restoreDocument(UUID id);

    Document archiveDocument(UUID id);

    Document changeClassification(UUID id, DocumentClassification classification);

    Document moveDocument(UUID id, UUID folderId);

    Document renameDocument(UUID id, String title);

    Document activateVersion(UUID documentId, UUID versionId);

    Document findById(UUID id);

    Document findByDocumentNumber(UUID organizationId, String documentNumber);

    Page<Document> findByOrganization(UUID organizationId, Pageable pageable);
}
