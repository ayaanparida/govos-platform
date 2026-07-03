package com.govos.doc.service;

import com.govos.doc.dto.CreateDocumentVersionRequest;
import com.govos.doc.dto.DocumentVersionDto;
import com.govos.doc.dto.UpdateDocumentVersionRequest;

import java.util.List;
import java.util.UUID;

public interface DocumentVersionService {

    DocumentVersionDto getById(UUID id);

    List<DocumentVersionDto> getByDocumentId(UUID documentId);

    DocumentVersionDto getByDocumentIdAndVersionNumber(UUID documentId, Integer versionNumber);

    DocumentVersionDto create(CreateDocumentVersionRequest request);

    DocumentVersionDto update(UUID id, UpdateDocumentVersionRequest request);

    void softDelete(UUID id);
}
