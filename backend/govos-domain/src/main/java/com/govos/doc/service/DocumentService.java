package com.govos.doc.service;

import com.govos.doc.dto.CreateDocumentRequest;
import com.govos.doc.dto.DocumentDto;
import com.govos.doc.dto.UpdateDocumentRequest;
import com.govos.doc.entity.DocumentStatus;

import java.util.List;
import java.util.UUID;

public interface DocumentService {

    DocumentDto getById(UUID id);

    DocumentDto getByCode(String code);

    List<DocumentDto> getAll();

    List<DocumentDto> getByFolderId(UUID folderId);

    List<DocumentDto> getByOwnerId(UUID ownerId);

    List<DocumentDto> getByStatus(DocumentStatus status);

    DocumentDto create(CreateDocumentRequest request);

    DocumentDto update(UUID id, UpdateDocumentRequest request);

    void softDelete(UUID id);
}
