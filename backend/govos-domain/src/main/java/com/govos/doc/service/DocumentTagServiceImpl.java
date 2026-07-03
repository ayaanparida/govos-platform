package com.govos.doc.service;

import com.govos.doc.dto.CreateDocumentTagRequest;
import com.govos.doc.dto.DocumentTagDto;
import com.govos.doc.dto.UpdateDocumentTagRequest;
import com.govos.doc.entity.DocumentTag;
import com.govos.doc.exception.DocumentTagNotFoundException;
import com.govos.doc.mapper.DocumentTagMapper;
import com.govos.doc.repository.DocumentTagRepository;
import com.govos.doc.validator.DocumentTagValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentTagServiceImpl implements DocumentTagService {

    private final DocumentTagRepository documentTagRepository;
    private final DocumentTagMapper documentTagMapper;
    private final DocumentTagValidator documentTagValidator;

    public DocumentTagServiceImpl(
            DocumentTagRepository documentTagRepository,
            DocumentTagMapper documentTagMapper,
            DocumentTagValidator documentTagValidator) {
        this.documentTagRepository = documentTagRepository;
        this.documentTagMapper = documentTagMapper;
        this.documentTagValidator = documentTagValidator;
    }

    @Override
    public DocumentTagDto getById(UUID id) {
        return documentTagMapper.toDto(findActiveById(id));
    }

    @Override
    public DocumentTagDto getByName(String name) {
        return documentTagMapper.toDto(documentTagRepository.findByNameAndDeletedFalse(name)
                .orElseThrow(() -> new DocumentTagNotFoundException(name)));
    }

    @Override
    public List<DocumentTagDto> getAll() {
        return documentTagRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(documentTagMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DocumentTagDto create(CreateDocumentTagRequest request) {
        documentTagValidator.validateCreate(request);

        DocumentTag entity = documentTagMapper.toEntity(request);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return documentTagMapper.toDto(documentTagRepository.save(entity));
    }

    @Override
    @Transactional
    public DocumentTagDto update(UUID id, UpdateDocumentTagRequest request) {
        DocumentTag entity = findActiveById(id);
        assertVersion(entity, request.version());
        documentTagValidator.validateUpdate(id, request);

        documentTagMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return documentTagMapper.toDto(documentTagRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        DocumentTag entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        documentTagRepository.save(entity);
    }

    private DocumentTag findActiveById(UUID id) {
        return documentTagRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DocumentTagNotFoundException(id));
    }

    private void assertVersion(DocumentTag entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "DocumentTag version mismatch for id: " + entity.getId());
        }
    }
}
