package com.govos.doc.service.impl;

import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.event.DocumentEvents;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.RetentionPolicyNotFoundException;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.mapper.DocumentRetentionPolicyMapper;
import com.govos.doc.repository.DocumentRetentionPolicyRepository;
import com.govos.doc.service.DocumentRetentionPolicyService;
import com.govos.doc.validator.DocumentRetentionPolicyValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentRetentionPolicyServiceImpl implements DocumentRetentionPolicyService {

    private final DocumentRetentionPolicyRepository retentionPolicyRepository;
    private final DocumentRetentionPolicyMapper retentionPolicyMapper;
    private final DocumentRetentionPolicyValidator retentionPolicyValidator;
    private final DocumentEventPublisher eventPublisher;

    public DocumentRetentionPolicyServiceImpl(
            DocumentRetentionPolicyRepository retentionPolicyRepository,
            DocumentRetentionPolicyMapper retentionPolicyMapper,
            DocumentRetentionPolicyValidator retentionPolicyValidator,
            DocumentEventPublisher eventPublisher) {
        this.retentionPolicyRepository = retentionPolicyRepository;
        this.retentionPolicyMapper = retentionPolicyMapper;
        this.retentionPolicyValidator = retentionPolicyValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DocumentRetentionPolicy createPolicy(CreateRetentionPolicyRequest request) {
        retentionPolicyValidator.validateCreate(request);
        assertUniquePolicyName(request.organizationId(), request.name(), null);

        DocumentRetentionPolicy entity = retentionPolicyMapper.toEntity(request);
        entity.setDeleted(false);
        entity.setActive(request.active() != null ? request.active() : true);
        if (Boolean.TRUE.equals(entity.getLegalHold()) && entity.getRetentionDays() != null && entity.getRetentionDays() <= 0) {
            ValidationResult result = new ValidationResult();
            result.addError(
                    "legalHold",
                    "Legal hold requires a positive retentionDays value",
                    "DOC_RETENTION_LEGAL_HOLD");
            result.throwIfInvalid();
        }

        DocumentRetentionPolicy saved = retentionPolicyRepository.save(entity);
        eventPublisher.publish(DocumentEvents.retentionPolicyCreated(saved));
        return saved;
    }

    @Override
    @Transactional
    public DocumentRetentionPolicy updatePolicy(UUID id, UpdateRetentionPolicyRequest request) {
        DocumentRetentionPolicy entity = findActiveById(id);
        assertVersion(entity, request.version());
        retentionPolicyValidator.validateUpdate(request);

        if (request.name() != null) {
            assertUniquePolicyName(entity.getOrganizationId(), request.name(), id);
        }

        retentionPolicyMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        DocumentRetentionPolicy saved = retentionPolicyRepository.save(entity);
        eventPublisher.publish(DocumentEvents.retentionPolicyUpdated(saved));
        return saved;
    }

    @Override
    @Transactional
    public void deletePolicy(UUID id) {
        DocumentRetentionPolicy entity = findActiveById(id);
        if (Boolean.TRUE.equals(entity.getLegalHold())) {
            ValidationResult result = new ValidationResult();
            result.addError("legalHold", "Cannot delete policy under legal hold", "DOC_RETENTION_LEGAL_HOLD_ACTIVE");
            result.throwIfInvalid();
        }
        entity.setDeleted(true);
        entity.setActive(false);
        DocumentRetentionPolicy saved = retentionPolicyRepository.save(entity);
        eventPublisher.publish(DocumentEvents.retentionPolicyDeleted(saved));
    }

    @Override
    @Transactional
    public DocumentRetentionPolicy restorePolicy(UUID id) {
        DocumentRetentionPolicy entity = retentionPolicyRepository.findById(id)
                .filter(policy -> Boolean.TRUE.equals(policy.getDeleted()))
                .orElseThrow(() -> new RetentionPolicyNotFoundException(id));
        entity.setDeleted(false);
        entity.setActive(true);
        DocumentRetentionPolicy saved = retentionPolicyRepository.save(entity);
        eventPublisher.publish(DocumentEvents.retentionPolicyRestored(saved));
        return saved;
    }

    @Override
    public DocumentRetentionPolicy findPolicy(UUID id) {
        return findActiveById(id);
    }

    private DocumentRetentionPolicy findActiveById(UUID id) {
        return retentionPolicyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RetentionPolicyNotFoundException(id));
    }

    private void assertUniquePolicyName(UUID organizationId, String name, UUID excludeId) {
        if (!StringUtils.hasText(name)) {
            return;
        }
        retentionPolicyRepository.findByOrganizationIdAndNameAndDeletedFalse(organizationId, name)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    ValidationResult result = new ValidationResult();
                    result.addError("name", "Retention policy name already exists", "DOC_DUPLICATE_POLICY_NAME");
                    result.throwIfInvalid();
                });
    }

    private void assertVersion(DocumentRetentionPolicy entity, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(entity.getVersion())) {
            throw new DocumentValidationException("Optimistic lock conflict for retention policy " + entity.getId());
        }
    }
}
