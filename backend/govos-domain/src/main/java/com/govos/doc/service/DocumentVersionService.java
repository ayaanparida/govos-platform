package com.govos.doc.service;

import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.entity.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DocumentVersionService {

    DocumentVersion createVersion(CreateDocumentVersionRequest request);

    DocumentVersion activateVersion(UUID versionId);

    DocumentVersion getLatestVersion(UUID documentId);

    DocumentVersion findVersion(UUID versionId);

    List<DocumentVersion> listVersions(UUID documentId);

    Page<DocumentVersion> listVersions(UUID documentId, Pageable pageable);
}
