package com.govos.doc.service.impl;

import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.event.DocumentEvents;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.StorageProviderNotFoundException;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.exception.VersionNotFoundException;
import com.govos.doc.mapper.DocumentVersionMapper;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentVersionRepository;
import com.govos.doc.repository.StorageProviderRepository;
import com.govos.doc.service.DocumentVersionService;
import com.govos.doc.validator.DocumentVersionValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final StorageProviderRepository storageProviderRepository;
    private final DocumentVersionMapper documentVersionMapper;
    private final DocumentVersionValidator documentVersionValidator;
    private final DocumentEventPublisher eventPublisher;

    public DocumentVersionServiceImpl(
            DocumentVersionRepository documentVersionRepository,
            DocumentRepository documentRepository,
            StorageProviderRepository storageProviderRepository,
            DocumentVersionMapper documentVersionMapper,
            DocumentVersionValidator documentVersionValidator,
            DocumentEventPublisher eventPublisher) {
        this.documentVersionRepository = documentVersionRepository;
        this.documentRepository = documentRepository;
        this.storageProviderRepository = storageProviderRepository;
        this.documentVersionMapper = documentVersionMapper;
        this.documentVersionValidator = documentVersionValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DocumentVersion createVersion(CreateDocumentVersionRequest request) {
        documentVersionValidator.validateCreate(request);
        Document document = documentRepository.findByIdAndDeletedFalse(request.documentId())
                .orElseThrow(() -> new DocumentNotFoundException(request.documentId()));
        assertSequentialVersionNumber(document.getId(), request.versionNumber());
        assertUniqueStorageKey(request.storageObjectKey(), null);

        StorageProvider storageProvider = storageProviderRepository.findByIdAndDeletedFalse(request.storageProviderId())
                .orElseThrow(() -> new StorageProviderNotFoundException(request.storageProviderId()));
        assertActiveProvider(storageProvider);

        DocumentVersion entity = documentVersionMapper.toEntity(request);
        entity.setDocument(document);
        entity.setStorageProvider(storageProvider);
        entity.setDeleted(false);
        entity.setActive(true);
        entity.setImmutable(true);
        if (entity.getVersionStatus() == null) {
            entity.setVersionStatus(DocumentVersionStatus.ACTIVE);
        }

        DocumentVersion saved = documentVersionRepository.save(entity);
        boolean activeVersionChanged = false;
        if (document.getActiveVersion() == null || entity.getVersionStatus() == DocumentVersionStatus.ACTIVE) {
            supersedeOtherVersions(document.getId(), saved.getId());
            document.setActiveVersion(saved);
            documentRepository.save(document);
            activeVersionChanged = true;
        }
        eventPublisher.publish(DocumentEvents.documentVersionCreated(saved));
        if (activeVersionChanged) {
            eventPublisher.publish(DocumentEvents.documentActiveVersionChanged(document));
        }
        return saved;
    }

    @Override
    @Transactional
    public DocumentVersion activateVersion(UUID versionId) {
        DocumentVersion version = findActiveVersion(versionId);
        Document document = version.getDocument();
        if (document == null) {
            throw new DocumentValidationException("Version is not linked to a document");
        }
        supersedeOtherVersions(document.getId(), versionId);
        version.setVersionStatus(DocumentVersionStatus.ACTIVE);
        document.setActiveVersion(version);
        documentRepository.save(document);
        DocumentVersion saved = documentVersionRepository.save(version);
        eventPublisher.publish(DocumentEvents.documentVersionActivated(saved));
        return saved;
    }

    @Override
    public DocumentVersion getLatestVersion(UUID documentId) {
        return documentVersionRepository.findTopByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(documentId)
                .orElseThrow(() -> new VersionNotFoundException(documentId));
    }

    @Override
    public DocumentVersion findVersion(UUID versionId) {
        return findActiveVersion(versionId);
    }

    @Override
    public List<DocumentVersion> listVersions(UUID documentId) {
        return documentVersionRepository.findByDocument_IdAndDeletedFalse(documentId);
    }

    @Override
    public Page<DocumentVersion> listVersions(UUID documentId, Pageable pageable) {
        return documentVersionRepository.findByDocument_IdAndDeletedFalse(documentId, pageable);
    }

    private DocumentVersion findActiveVersion(UUID versionId) {
        return documentVersionRepository.findByIdAndDeletedFalse(versionId)
                .orElseThrow(() -> new VersionNotFoundException(versionId));
    }

    private void assertSequentialVersionNumber(UUID documentId, Integer requestedVersionNumber) {
        int nextVersion = documentVersionRepository
                .findTopByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(documentId)
                .map(existing -> existing.getVersionNumber().getValue() + 1)
                .orElse(1);
        if (!Integer.valueOf(nextVersion).equals(requestedVersionNumber)) {
            ValidationResult result = new ValidationResult();
            result.addError(
                    "versionNumber",
                    "Expected version number " + nextVersion + " but received " + requestedVersionNumber,
                    "DOC_VERSION_SEQUENCE");
            result.throwIfInvalid();
        }
    }

    private void assertUniqueStorageKey(String storageObjectKey, UUID excludeId) {
        documentVersionRepository.findByStorageLocation_StorageObjectKeyAndDeletedFalse(storageObjectKey)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    ValidationResult result = new ValidationResult();
                    result.addError(
                            "storageObjectKey",
                            "Storage object key already exists",
                            "DOC_DUPLICATE_STORAGE_KEY");
                    result.throwIfInvalid();
                });
    }

    private void assertActiveProvider(StorageProvider storageProvider) {
        if (!Boolean.TRUE.equals(storageProvider.getActive())) {
            throw new DocumentValidationException("Storage provider must be active: " + storageProvider.getId());
        }
    }

    private void supersedeOtherVersions(UUID documentId, UUID activeVersionId) {
        List<DocumentVersion> versions = documentVersionRepository.findByDocument_IdAndDeletedFalse(documentId);
        for (DocumentVersion version : versions) {
            if (!version.getId().equals(activeVersionId)
                    && version.getVersionStatus() == DocumentVersionStatus.ACTIVE) {
                version.setVersionStatus(DocumentVersionStatus.SUPERSEDED);
                documentVersionRepository.save(version);
            }
        }
    }
}
