package com.govos.doc.service;

import com.govos.doc.dto.CreateDocumentVersionRequest;
import com.govos.doc.dto.DocumentVersionDto;
import com.govos.doc.dto.UpdateDocumentVersionRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentVersionNotFoundException;
import com.govos.doc.mapper.DocumentVersionMapper;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentVersionRepository;
import com.govos.doc.validator.DocumentVersionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionMapper documentVersionMapper;
    private final DocumentVersionValidator documentVersionValidator;

    public DocumentVersionServiceImpl(
            DocumentVersionRepository documentVersionRepository,
            DocumentRepository documentRepository,
            DocumentVersionMapper documentVersionMapper,
            DocumentVersionValidator documentVersionValidator) {
        this.documentVersionRepository = documentVersionRepository;
        this.documentRepository = documentRepository;
        this.documentVersionMapper = documentVersionMapper;
        this.documentVersionValidator = documentVersionValidator;
    }

    @Override
    public DocumentVersionDto getById(UUID id) {
        return documentVersionMapper.toDto(findActiveById(id));
    }

    @Override
    public List<DocumentVersionDto> getByDocumentId(UUID documentId) {
        return documentVersionRepository.findByDocument_IdAndDeletedFalseOrderByVersionNumberDesc(documentId).stream()
                .map(documentVersionMapper::toDto)
                .toList();
    }

    @Override
    public DocumentVersionDto getByDocumentIdAndVersionNumber(UUID documentId, Integer versionNumber) {
        return documentVersionMapper.toDto(
                documentVersionRepository.findByDocument_IdAndVersionNumberAndDeletedFalse(documentId, versionNumber)
                        .orElseThrow(() -> new DocumentVersionNotFoundException(documentId, versionNumber)));
    }

    @Override
    @Transactional
    public DocumentVersionDto create(CreateDocumentVersionRequest request) {
        documentVersionValidator.validateCreate(request);

        Document document = documentRepository.findByIdAndDeletedFalse(request.documentId())
                .orElseThrow(() -> new DocumentNotFoundException(request.documentId()));

        DocumentVersion entity = new DocumentVersion();
        entity.setCode(request.code());
        entity.setDocument(document);
        entity.setVersionNumber(request.versionNumber());
        entity.setChecksum(request.checksum());
        entity.setSize(request.size());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return documentVersionMapper.toDto(documentVersionRepository.save(entity));
    }

    @Override
    @Transactional
    public DocumentVersionDto update(UUID id, UpdateDocumentVersionRequest request) {
        DocumentVersion entity = findActiveById(id);
        assertVersion(entity, request.version());
        documentVersionValidator.validateUpdate(id, entity.getDocument().getId(), request);

        entity.setCode(request.code());
        entity.setVersionNumber(request.versionNumber());
        entity.setChecksum(request.checksum());
        entity.setSize(request.size());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return documentVersionMapper.toDto(documentVersionRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        DocumentVersion entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        documentVersionRepository.save(entity);
    }

    private DocumentVersion findActiveById(UUID id) {
        return documentVersionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DocumentVersionNotFoundException(id));
    }

    private void assertVersion(DocumentVersion entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "DocumentVersion version mismatch for id: " + entity.getId());
        }
    }
}
