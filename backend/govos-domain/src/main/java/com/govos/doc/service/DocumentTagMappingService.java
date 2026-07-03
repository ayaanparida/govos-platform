package com.govos.doc.service;

import com.govos.doc.dto.CreateDocumentTagMappingRequest;
import com.govos.doc.dto.DocumentTagMappingDto;

import java.util.List;
import java.util.UUID;

public interface DocumentTagMappingService {

    DocumentTagMappingDto getById(UUID id);

    List<DocumentTagMappingDto> getByDocumentId(UUID documentId);

    List<DocumentTagMappingDto> getByTagId(UUID tagId);

    DocumentTagMappingDto create(CreateDocumentTagMappingRequest request);

    void remove(UUID id);
}
