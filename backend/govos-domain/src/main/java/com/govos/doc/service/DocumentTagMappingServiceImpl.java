package com.govos.doc.service;

import com.govos.doc.dto.CreateDocumentTagMappingRequest;
import com.govos.doc.dto.DocumentTagMappingDto;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentTag;
import com.govos.doc.entity.DocumentTagMapping;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentTagMappingNotFoundException;
import com.govos.doc.exception.DocumentTagNotFoundException;
import com.govos.doc.mapper.DocumentTagMappingMapper;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentTagMappingRepository;
import com.govos.doc.repository.DocumentTagRepository;
import com.govos.doc.validator.DocumentTagMappingValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentTagMappingServiceImpl implements DocumentTagMappingService {

    private final DocumentTagMappingRepository documentTagMappingRepository;
    private final DocumentRepository documentRepository;
    private final DocumentTagRepository documentTagRepository;
    private final DocumentTagMappingMapper documentTagMappingMapper;
    private final DocumentTagMappingValidator documentTagMappingValidator;

    public DocumentTagMappingServiceImpl(
            DocumentTagMappingRepository documentTagMappingRepository,
            DocumentRepository documentRepository,
            DocumentTagRepository documentTagRepository,
            DocumentTagMappingMapper documentTagMappingMapper,
            DocumentTagMappingValidator documentTagMappingValidator) {
        this.documentTagMappingRepository = documentTagMappingRepository;
        this.documentRepository = documentRepository;
        this.documentTagRepository = documentTagRepository;
        this.documentTagMappingMapper = documentTagMappingMapper;
        this.documentTagMappingValidator = documentTagMappingValidator;
    }

    @Override
    public DocumentTagMappingDto getById(UUID id) {
        return documentTagMappingMapper.toDto(findActiveById(id));
    }

    @Override
    public List<DocumentTagMappingDto> getByDocumentId(UUID documentId) {
        return documentTagMappingRepository.findByDocument_IdAndDeletedFalse(documentId).stream()
                .map(documentTagMappingMapper::toDto)
                .toList();
    }

    @Override
    public List<DocumentTagMappingDto> getByTagId(UUID tagId) {
        return documentTagMappingRepository.findByTag_IdAndDeletedFalse(tagId).stream()
                .map(documentTagMappingMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DocumentTagMappingDto create(CreateDocumentTagMappingRequest request) {
        documentTagMappingValidator.validateCreate(request);

        Document document = documentRepository.findByIdAndDeletedFalse(request.documentId())
                .orElseThrow(() -> new DocumentNotFoundException(request.documentId()));
        DocumentTag tag = documentTagRepository.findByIdAndDeletedFalse(request.tagId())
                .orElseThrow(() -> new DocumentTagNotFoundException(request.tagId()));

        DocumentTagMapping entity = new DocumentTagMapping();
        entity.setCode(request.code());
        entity.setDocument(document);
        entity.setTag(tag);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return documentTagMappingMapper.toDto(documentTagMappingRepository.save(entity));
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        DocumentTagMapping entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        documentTagMappingRepository.save(entity);
    }

    private DocumentTagMapping findActiveById(UUID id) {
        return documentTagMappingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DocumentTagMappingNotFoundException(id));
    }
}
