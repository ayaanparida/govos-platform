package com.govos.doc.service.impl;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
import com.govos.doc.entity.DocumentCategory;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.event.DocumentEvents;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.CategoryNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.RetentionPolicyNotFoundException;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.mapper.DocumentCategoryMapper;
import com.govos.doc.repository.DocumentCategoryRepository;
import com.govos.doc.repository.DocumentRetentionPolicyRepository;
import com.govos.doc.service.DocumentCategoryService;
import com.govos.doc.validator.DocumentCategoryValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentCategoryServiceImpl implements DocumentCategoryService {

    private final DocumentCategoryRepository categoryRepository;
    private final DocumentRetentionPolicyRepository retentionPolicyRepository;
    private final DocumentCategoryMapper categoryMapper;
    private final DocumentCategoryValidator categoryValidator;
    private final DocumentEventPublisher eventPublisher;

    public DocumentCategoryServiceImpl(
            DocumentCategoryRepository categoryRepository,
            DocumentRetentionPolicyRepository retentionPolicyRepository,
            DocumentCategoryMapper categoryMapper,
            DocumentCategoryValidator categoryValidator,
            DocumentEventPublisher eventPublisher) {
        this.categoryRepository = categoryRepository;
        this.retentionPolicyRepository = retentionPolicyRepository;
        this.categoryMapper = categoryMapper;
        this.categoryValidator = categoryValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DocumentCategory createCategory(CreateDocumentCategoryRequest request) {
        categoryValidator.validateCreate(request);
        assertUniqueCode(request.organizationId(), request.code(), null);

        DocumentCategory entity = categoryMapper.toEntity(request);
        entity.setDeleted(false);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setCode(request.code());
        entity.setParentCategory(resolveParent(request.parentCategoryId(), request.organizationId()));
        entity.setDefaultRetentionPolicy(resolveRetentionPolicy(request.defaultRetentionPolicyId()));

        DocumentCategory saved = categoryRepository.save(entity);
        eventPublisher.publish(DocumentEvents.categoryCreated(saved));
        return saved;
    }

    @Override
    @Transactional
    public DocumentCategory updateCategory(UUID id, UpdateDocumentCategoryRequest request) {
        DocumentCategory entity = findActiveById(id);
        assertVersion(entity, request.version());
        categoryValidator.validateUpdate(request, id);

        categoryMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        if (request.parentCategoryId() != null) {
            entity.setParentCategory(resolveParent(request.parentCategoryId(), entity.getOrganizationId()));
        }
        if (request.defaultRetentionPolicyId() != null) {
            entity.setDefaultRetentionPolicy(resolveRetentionPolicy(request.defaultRetentionPolicyId()));
        }

        DocumentCategory saved = categoryRepository.save(entity);
        eventPublisher.publish(DocumentEvents.categoryUpdated(saved));
        return saved;
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        DocumentCategory entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        DocumentCategory saved = categoryRepository.save(entity);
        eventPublisher.publish(DocumentEvents.categoryDeleted(saved));
    }

    @Override
    @Transactional
    public DocumentCategory restoreCategory(UUID id) {
        DocumentCategory entity = categoryRepository.findById(id)
                .filter(category -> Boolean.TRUE.equals(category.getDeleted()))
                .orElseThrow(() -> new CategoryNotFoundException(id));
        entity.setDeleted(false);
        entity.setActive(true);
        DocumentCategory saved = categoryRepository.save(entity);
        eventPublisher.publish(DocumentEvents.categoryRestored(saved));
        return saved;
    }

    private DocumentCategory findActiveById(UUID id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private DocumentCategory resolveParent(UUID parentCategoryId, UUID organizationId) {
        if (parentCategoryId == null) {
            return null;
        }
        DocumentCategory parent = findActiveById(parentCategoryId);
        if (parent.getOrganizationId() != null && organizationId != null
                && !parent.getOrganizationId().equals(organizationId)) {
            throw new DocumentValidationException("Parent category does not belong to organization " + organizationId);
        }
        return parent;
    }

    private DocumentRetentionPolicy resolveRetentionPolicy(UUID policyId) {
        if (policyId == null) {
            return null;
        }
        return retentionPolicyRepository.findByIdAndDeletedFalse(policyId)
                .orElseThrow(() -> new RetentionPolicyNotFoundException(policyId));
    }

    private void assertUniqueCode(UUID organizationId, String code, UUID excludeId) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        categoryRepository.findByOrganizationIdAndCodeAndDeletedFalse(organizationId, code)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    ValidationResult result = new ValidationResult();
                    result.addError("code", "Category code already exists in organization", "DOC_DUPLICATE_CATEGORY_CODE");
                    result.throwIfInvalid();
                });
    }

    private void assertVersion(DocumentCategory entity, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(entity.getVersion())) {
            throw new DocumentValidationException("Optimistic lock conflict for category " + entity.getId());
        }
    }
}
