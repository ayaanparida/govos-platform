package com.govos.audit.validator;

import com.govos.audit.dto.CreateAuditChangeRequest;
import com.govos.audit.exception.AuditValidationException;
import com.govos.audit.repository.AuditChangeRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditChangeValidator {

    private final AuditChangeRepository auditChangeRepository;

    public AuditChangeValidator(AuditChangeRepository auditChangeRepository) {
        this.auditChangeRepository = auditChangeRepository;
    }

    public void validateCreate(CreateAuditChangeRequest request) {
        if (auditChangeRepository.existsByAuditEvent_IdAndFieldNameAndDeletedFalse(
                request.auditEventId(), request.fieldName())) {
            throw new AuditValidationException(
                    "Audit change field already exists for event: " + request.auditEventId()
                            + ", fieldName=" + request.fieldName());
        }
    }
}
