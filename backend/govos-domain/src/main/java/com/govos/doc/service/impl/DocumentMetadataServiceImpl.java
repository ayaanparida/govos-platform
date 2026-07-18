package com.govos.doc.service.impl;

import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentMetadata;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.event.DocumentEvents;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.MetadataNotFoundException;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.exception.VersionNotFoundException;
import com.govos.doc.mapper.DocumentMetadataMapper;
import com.govos.doc.repository.DocumentMetadataRepository;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentVersionRepository;
import com.govos.doc.service.DocumentMetadataService;
import com.govos.doc.validator.DocumentMetadataValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentMetadataServiceImpl implements DocumentMetadataService {

    private final DocumentMetadataRepository metadataRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentMetadataMapper metadataMapper;
    private final DocumentMetadataValidator metadataValidator;
    private final DocumentEventPublisher eventPublisher;

    public DocumentMetadataServiceImpl(
            DocumentMetadataRepository metadataRepository,
            DocumentRepository documentRepository,
            DocumentVersionRepository documentVersionRepository,
            DocumentMetadataMapper metadataMapper,
            DocumentMetadataValidator metadataValidator,
            DocumentEventPublisher eventPublisher) {
        this.metadataRepository = metadataRepository;
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.metadataMapper = metadataMapper;
        this.metadataValidator = metadataValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DocumentMetadata createMetadata(
            UUID documentId,
            UUID documentVersionId,
            UpdateDocumentMetadataRequest request) {
        metadataValidator.validateDocumentScope(documentId, documentVersionId);
        if (request != null) {
            metadataValidator.validateUpdate(request);
        }
        Document document = documentRepository.findByIdAndDeletedFalse(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        DocumentVersion version = resolveVersion(documentVersionId, documentId);
        assertSingleMetadataScope(documentId, documentVersionId, null);

        DocumentMetadata entity = new DocumentMetadata();
        entity.setDocument(document);
        entity.setDocumentVersion(version);
        entity.setDeleted(false);
        entity.setActive(true);
        entity.setWatermarkApplied(false);
        if (request != null) {
            metadataMapper.updateEntity(request, entity);
            if (request.active() != null) {
                entity.setActive(request.active());
            }
            if (request.watermarkApplied() != null) {
                entity.setWatermarkApplied(request.watermarkApplied());
            }
        }

        DocumentMetadata saved = metadataRepository.save(entity);
        eventPublisher.publish(DocumentEvents.metadataCreated(saved));
        return saved;
    }

    @Override
    @Transactional
    public DocumentMetadata updateMetadata(UUID id, UpdateDocumentMetadataRequest request) {
        DocumentMetadata entity = findActiveById(id);
        assertVersion(entity, request.version());
        metadataValidator.validateUpdate(request);
        metadataMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        DocumentMetadata saved = metadataRepository.save(entity);
        eventPublisher.publish(DocumentEvents.metadataUpdated(saved));
        return saved;
    }

    @Override
    @Transactional
    public DocumentMetadata replaceMetadata(UUID id, UpdateDocumentMetadataRequest request) {
        DocumentMetadata entity = findActiveById(id);
        assertVersion(entity, request.version());
        metadataValidator.validateUpdate(request);
        applyFullReplacement(entity, request);
        DocumentMetadata saved = metadataRepository.save(entity);
        eventPublisher.publish(DocumentEvents.metadataUpdated(saved));
        return saved;
    }

    @Override
    public DocumentMetadata findMetadata(UUID documentId, UUID documentVersionId) {
        metadataValidator.validateDocumentScope(documentId, documentVersionId);
        List<DocumentMetadata> entries = documentVersionId != null
                ? metadataRepository.findByDocumentVersion_IdAndDeletedFalse(documentVersionId)
                : metadataRepository.findByDocument_IdAndDeletedFalse(documentId).stream()
                        .filter(metadata -> metadata.getDocumentVersion() == null)
                        .toList();
        return entries.stream()
                .findFirst()
                .orElseThrow(() -> new MetadataNotFoundException(documentId));
    }

    private DocumentMetadata findActiveById(UUID id) {
        return metadataRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new MetadataNotFoundException(id));
    }

    private DocumentVersion resolveVersion(UUID documentVersionId, UUID documentId) {
        if (documentVersionId == null) {
            return null;
        }
        DocumentVersion version = documentVersionRepository.findByIdAndDeletedFalse(documentVersionId)
                .orElseThrow(() -> new VersionNotFoundException(documentVersionId));
        if (version.getDocument() == null || !documentId.equals(version.getDocument().getId())) {
            throw new DocumentValidationException("Document version does not belong to document " + documentId);
        }
        return version;
    }

    private void assertSingleMetadataScope(UUID documentId, UUID documentVersionId, UUID excludeId) {
        List<DocumentMetadata> existing = documentVersionId != null
                ? metadataRepository.findByDocumentVersion_IdAndDeletedFalse(documentVersionId)
                : metadataRepository.findByDocument_IdAndDeletedFalse(documentId).stream()
                        .filter(metadata -> metadata.getDocumentVersion() == null)
                        .toList();
        boolean duplicate = existing.stream()
                .anyMatch(metadata -> excludeId == null || !metadata.getId().equals(excludeId));
        if (duplicate) {
            ValidationResult result = new ValidationResult();
            result.addError(
                    "documentVersionId",
                    "Metadata already exists for this document version scope",
                    "DOC_DUPLICATE_METADATA");
            result.throwIfInvalid();
        }
    }

    private void applyFullReplacement(DocumentMetadata entity, UpdateDocumentMetadataRequest request) {
        entity.setOcrText(request.ocrText());
        entity.setOcrLanguage(request.ocrLanguage());
        entity.setOcrConfidence(request.ocrConfidence());
        entity.setExtractedMetadata(request.extractedMetadata());
        entity.setCustomAttributes(request.customAttributes());
        entity.setPageCount(request.pageCount());
        entity.setLanguageDetected(request.languageDetected());
        if (request.watermarkApplied() != null) {
            entity.setWatermarkApplied(request.watermarkApplied());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }

    private void assertVersion(DocumentMetadata entity, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(entity.getVersion())) {
            throw new DocumentValidationException("Optimistic lock conflict for metadata " + entity.getId());
        }
    }
}
