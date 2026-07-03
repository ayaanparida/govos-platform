package com.govos.doc.service;

import com.govos.doc.dto.CreateDocumentTagRequest;
import com.govos.doc.dto.DocumentTagDto;
import com.govos.doc.dto.UpdateDocumentTagRequest;

import java.util.List;
import java.util.UUID;

public interface DocumentTagService {

    DocumentTagDto getById(UUID id);

    DocumentTagDto getByName(String name);

    List<DocumentTagDto> getAll();

    DocumentTagDto create(CreateDocumentTagRequest request);

    DocumentTagDto update(UUID id, UpdateDocumentTagRequest request);

    void softDelete(UUID id);
}
