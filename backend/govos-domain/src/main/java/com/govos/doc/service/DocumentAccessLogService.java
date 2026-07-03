package com.govos.doc.service;

import com.govos.doc.dto.CreateDocumentAccessLogRequest;
import com.govos.doc.dto.DocumentAccessLogDto;
import com.govos.doc.entity.DocumentAccessAction;

import java.util.List;
import java.util.UUID;

public interface DocumentAccessLogService {

    DocumentAccessLogDto getById(UUID id);

    List<DocumentAccessLogDto> getByDocumentId(UUID documentId);

    List<DocumentAccessLogDto> getByUserId(UUID userId);

    List<DocumentAccessLogDto> getByDocumentIdAndAction(UUID documentId, DocumentAccessAction action);

    DocumentAccessLogDto create(CreateDocumentAccessLogRequest request);
}
