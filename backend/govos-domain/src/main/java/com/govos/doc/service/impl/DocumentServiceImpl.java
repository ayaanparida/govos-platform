package com.govos.doc.service.impl;

import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentCategory;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.entity.Folder;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.exception.CategoryNotFoundException;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.FolderNotFoundException;
import com.govos.doc.exception.RetentionPolicyNotFoundException;
import com.govos.doc.event.DocumentEvents;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.mapper.DocumentMapper;
import com.govos.doc.repository.DocumentCategoryRepository;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentRetentionPolicyRepository;
import com.govos.doc.repository.DocumentVersionRepository;
import com.govos.doc.repository.FolderRepository;
import com.govos.doc.service.DocumentService;
import com.govos.doc.service.DocumentVersionService;
import com.govos.doc.validator.DocumentValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final DocumentRetentionPolicyRepository retentionPolicyRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentMapper documentMapper;
    private final DocumentValidator documentValidator;
    private final DocumentVersionService documentVersionService;
    private final DocumentEventPublisher eventPublisher;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            FolderRepository folderRepository,
            DocumentCategoryRepository categoryRepository,
            DocumentRetentionPolicyRepository retentionPolicyRepository,
            DocumentVersionRepository documentVersionRepository,
            DocumentMapper documentMapper,
            DocumentValidator documentValidator,
            DocumentVersionService documentVersionService,
            DocumentEventPublisher eventPublisher) {
        this.documentRepository = documentRepository;
        this.folderRepository = folderRepository;
        this.categoryRepository = categoryRepository;
        this.retentionPolicyRepository = retentionPolicyRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.documentMapper = documentMapper;
        this.documentValidator = documentValidator;
        this.documentVersionService = documentVersionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Document createDocument(CreateDocumentRequest request) {
        documentValidator.validateCreate(request);
        assertUniqueDocumentNumber(request.organizationId(), request.documentNumber(), null);

        Document entity = documentMapper.toEntity(request);
        entity.setDeleted(false);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setStatus(DocumentStatus.UPLOADED);
        entity.setFolder(resolveFolder(request.folderId(), request.organizationId()));
        entity.setCategory(resolveCategory(request.categoryId(), request.organizationId()));
        entity.setRetentionPolicy(resolveRetentionPolicy(request.retentionPolicyId(), request.organizationId()));

        Document saved = documentRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentCreated(saved));
        return saved;
    }

    @Override
    @Transactional
    public Document updateDocument(UUID id, UpdateDocumentRequest request) {
        Document entity = findActiveById(id);
        assertVersion(entity, request.version());
        documentValidator.validateUpdate(request);

        if (request.documentNumber() != null) {
            assertUniqueDocumentNumber(entity.getOrganizationId(), request.documentNumber(), id);
        }

        documentMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        if (request.status() != null) {
            documentValidator.validateStatusTransition(entity.getStatus(), request.status());
            entity.setStatus(request.status());
        }
        if (request.folderId() != null) {
            entity.setFolder(resolveFolder(request.folderId(), entity.getOrganizationId()));
        }
        if (request.categoryId() != null) {
            entity.setCategory(resolveCategory(request.categoryId(), entity.getOrganizationId()));
        }
        if (request.retentionPolicyId() != null) {
            entity.setRetentionPolicy(resolveRetentionPolicy(request.retentionPolicyId(), entity.getOrganizationId()));
        }

        Document saved = documentRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentUpdated(saved));
        return saved;
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        Document entity = findActiveById(id);
        documentValidator.validateDelete(id);
        documentValidator.validateStatusTransition(entity.getStatus(), DocumentStatus.DELETED);
        entity.setStatus(DocumentStatus.DELETED);
        entity.setDeleted(true);
        entity.setActive(false);
        Document saved = documentRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentDeleted(saved));
    }

    @Override
    @Transactional
    public Document restoreDocument(UUID id) {
        Document entity = documentRepository.findById(id)
                .filter(document -> Boolean.TRUE.equals(document.getDeleted()))
                .orElseThrow(() -> new DocumentNotFoundException(id));
        documentValidator.validateRestore(entity.getStatus());
        entity.setDeleted(false);
        entity.setActive(true);
        entity.setStatus(DocumentStatus.UPLOADED);
        Document saved = documentRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentRestored(saved));
        return saved;
    }

    @Override
    @Transactional
    public Document archiveDocument(UUID id) {
        Document entity = findActiveById(id);
        documentValidator.validateStatusTransition(entity.getStatus(), DocumentStatus.ARCHIVED);
        entity.setStatus(DocumentStatus.ARCHIVED);
        Document saved = documentRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentArchived(saved));
        return saved;
    }

    @Override
    @Transactional
    public Document changeClassification(UUID id, DocumentClassification classification) {
        Document entity = findActiveById(id);
        ValidationResult result = new ValidationResult();
        com.govos.doc.validator.ValidationUtils.requireEnum(result, "classification", classification);
        result.throwIfInvalid();
        entity.setClassification(classification);
        Document saved = documentRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentClassificationChanged(saved, classification));
        return saved;
    }

    @Override
    @Transactional
    public Document moveDocument(UUID id, UUID folderId) {
        Document entity = findActiveById(id);
        entity.setFolder(resolveFolder(folderId, entity.getOrganizationId()));
        Document saved = documentRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentMoved(saved));
        return saved;
    }

    @Override
    @Transactional
    public Document renameDocument(UUID id, String title) {
        Document entity = findActiveById(id);
        ValidationResult result = new ValidationResult();
        com.govos.doc.validator.ValidationUtils.requireText(result, "title", title);
        com.govos.doc.validator.ValidationUtils.requireMaxLength(
                result, "title", title, com.govos.doc.validator.ValidationUtils.MAX_TITLE_LENGTH);
        result.throwIfInvalid();
        entity.setTitle(title);
        Document saved = documentRepository.save(entity);
        eventPublisher.publish(DocumentEvents.documentRenamed(saved));
        return saved;
    }

    @Override
    @Transactional
    public Document activateVersion(UUID documentId, UUID versionId) {
        findActiveById(documentId);
        DocumentVersion version = documentVersionRepository.findByIdAndDeletedFalse(versionId)
                .orElseThrow(() -> new com.govos.doc.exception.VersionNotFoundException(versionId));
        assertVersionBelongsToDocument(version, documentId);
        documentVersionService.activateVersion(versionId);
        Document saved = findActiveById(documentId);
        eventPublisher.publish(DocumentEvents.documentActiveVersionChanged(saved));
        return saved;
    }

    @Override
    public Document findById(UUID id) {
        return findActiveById(id);
    }

    @Override
    public Document findByDocumentNumber(UUID organizationId, String documentNumber) {
        return documentRepository.findByOrganizationIdAndDocumentNumberAndDeletedFalse(organizationId, documentNumber)
                .orElseThrow(() -> new DocumentNotFoundException(organizationId, documentNumber));
    }

    @Override
    public Page<Document> findByOrganization(UUID organizationId, Pageable pageable) {
        return documentRepository.findByOrganizationIdAndDeletedFalse(organizationId, pageable);
    }

    private Document findActiveById(UUID id) {
        return documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    private void assertUniqueDocumentNumber(UUID organizationId, String documentNumber, UUID excludeId) {
        if (!StringUtils.hasText(documentNumber)) {
            return;
        }
        documentRepository.findByOrganizationIdAndDocumentNumberAndDeletedFalse(organizationId, documentNumber)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    ValidationResult result = new ValidationResult();
                    result.addError(
                            "documentNumber",
                            "Document number already exists in organization",
                            "DOC_DUPLICATE_DOCUMENT_NUMBER");
                    result.throwIfInvalid();
                });
    }

    private Folder resolveFolder(UUID folderId, UUID organizationId) {
        if (folderId == null) {
            return null;
        }
        Folder folder = folderRepository.findByIdAndDeletedFalse(folderId)
                .orElseThrow(() -> new FolderNotFoundException(folderId));
        assertOrganizationScope(folder.getOrganizationId(), organizationId, "folderId");
        return folder;
    }

    private DocumentCategory resolveCategory(UUID categoryId, UUID organizationId) {
        if (categoryId == null) {
            return null;
        }
        DocumentCategory category = categoryRepository.findByIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        if (category.getOrganizationId() != null) {
            assertOrganizationScope(category.getOrganizationId(), organizationId, "categoryId");
        }
        return category;
    }

    private DocumentRetentionPolicy resolveRetentionPolicy(UUID policyId, UUID organizationId) {
        if (policyId == null) {
            return null;
        }
        DocumentRetentionPolicy policy = retentionPolicyRepository.findByIdAndDeletedFalse(policyId)
                .orElseThrow(() -> new RetentionPolicyNotFoundException(policyId));
        if (policy.getOrganizationId() != null) {
            assertOrganizationScope(policy.getOrganizationId(), organizationId, "retentionPolicyId");
        }
        return policy;
    }

    private void assertOrganizationScope(UUID entityOrganizationId, UUID expectedOrganizationId, String field) {
        if (!entityOrganizationId.equals(expectedOrganizationId)) {
            throw new DocumentValidationException(field + " does not belong to organization " + expectedOrganizationId);
        }
    }

    private void assertVersionBelongsToDocument(DocumentVersion version, UUID documentId) {
        if (version.getDocument() == null || !documentId.equals(version.getDocument().getId())) {
            throw new DocumentValidationException("Version does not belong to document " + documentId);
        }
    }

    private void assertVersion(Document entity, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(entity.getVersion())) {
            throw new DocumentValidationException("Optimistic lock conflict for document " + entity.getId());
        }
    }
}
