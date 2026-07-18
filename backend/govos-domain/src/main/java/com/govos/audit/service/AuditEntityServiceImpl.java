package com.govos.audit.service;

import com.govos.audit.dto.AuditEntityDto;
import com.govos.audit.dto.CreateAuditEntityRequest;
import com.govos.audit.dto.UpdateAuditEntityRequest;
import com.govos.audit.entity.AuditEntity;
import com.govos.audit.exception.AuditEntityNotFoundException;
import com.govos.audit.mapper.AuditEntityMapper;
import com.govos.audit.repository.AuditEntityRepository;
import com.govos.audit.validator.AuditEntityValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditEntityServiceImpl implements AuditEntityService {

    private final AuditEntityRepository auditEntityRepository;
    private final AuditEntityMapper auditEntityMapper;
    private final AuditEntityValidator auditEntityValidator;

    public AuditEntityServiceImpl(
            AuditEntityRepository auditEntityRepository,
            AuditEntityMapper auditEntityMapper,
            AuditEntityValidator auditEntityValidator) {
        this.auditEntityRepository = auditEntityRepository;
        this.auditEntityMapper = auditEntityMapper;
        this.auditEntityValidator = auditEntityValidator;
    }

    @Override
    public AuditEntityDto getById(UUID id) {
        return auditEntityMapper.toDto(findActiveById(id));
    }

    @Override
    public AuditEntityDto getByEntityTypeAndId(String entityType, UUID entityId) {
        return auditEntityMapper.toDto(findActiveByEntityTypeAndId(entityType, entityId));
    }

    @Override
    public List<AuditEntityDto> getByEntityType(String entityType) {
        return auditEntityRepository.findByEntityTypeAndDeletedFalseOrderByEntityNameAsc(entityType).stream()
                .map(auditEntityMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AuditEntityDto create(CreateAuditEntityRequest request) {
        auditEntityValidator.validateCreate(request);

        AuditEntity entity = new AuditEntity();
        entity.setCode(request.code());
        entity.setEntityType(request.entityType());
        entity.setEntityId(request.entityId());
        entity.setEntityName(request.entityName());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return auditEntityMapper.toDto(auditEntityRepository.save(entity));
    }

    @Override
    @Transactional
    public AuditEntityDto update(UUID id, UpdateAuditEntityRequest request) {
        AuditEntity entity = findActiveById(id);
        assertVersion(entity, request.version());
        auditEntityValidator.validateUpdate(id, request);

        if (request.code() != null) {
            entity.setCode(request.code());
        }
        entity.setEntityType(request.entityType());
        entity.setEntityId(request.entityId());
        entity.setEntityName(request.entityName());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return auditEntityMapper.toDto(auditEntityRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        AuditEntity entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        auditEntityRepository.save(entity);
    }

    private AuditEntity findActiveById(UUID id) {
        return auditEntityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuditEntityNotFoundException(id));
    }

    private AuditEntity findActiveByEntityTypeAndId(String entityType, UUID entityId) {
        return auditEntityRepository.findByEntityTypeAndEntityIdAndDeletedFalse(entityType, entityId)
                .orElseThrow(() -> new AuditEntityNotFoundException(entityType, entityId));
    }

    private void assertVersion(AuditEntity entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "AuditEntity version mismatch for id: " + entity.getId());
        }
    }
}
