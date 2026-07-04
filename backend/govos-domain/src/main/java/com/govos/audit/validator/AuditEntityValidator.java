package com.govos.audit.validator;

import com.govos.audit.dto.CreateAuditEntityRequest;
import com.govos.audit.dto.UpdateAuditEntityRequest;
import com.govos.audit.exception.AuditValidationException;
import com.govos.audit.repository.AuditEntityRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditEntityValidator {

    private final AuditEntityRepository auditEntityRepository;

    public AuditEntityValidator(AuditEntityRepository auditEntityRepository) {
        this.auditEntityRepository = auditEntityRepository;
    }

    public void validateCreate(CreateAuditEntityRequest request) {
        if (auditEntityRepository.existsByEntityTypeAndEntityIdAndDeletedFalse(
                request.entityType(), request.entityId())) {
            throw new AuditValidationException(
                    "Audit entity already exists for type: " + request.entityType()
                            + ", entityId=" + request.entityId());
        }
    }

    public void validateUpdate(UUID id, UpdateAuditEntityRequest request) {
        auditEntityRepository
                .findByEntityTypeAndEntityIdAndDeletedFalse(request.entityType(), request.entityId())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new AuditValidationException(
                            "Audit entity already exists for type: " + request.entityType()
                                    + ", entityId=" + request.entityId());
                });
    }
}
