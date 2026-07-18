package com.govos.audit.validator;

import com.govos.audit.dto.CreateAuditEventRequest;
import com.govos.audit.exception.DuplicateAuditEventException;
import com.govos.audit.repository.AuditEventRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditEventValidator {

    private final AuditEventRepository auditEventRepository;

    public AuditEventValidator(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void validateCreate(CreateAuditEventRequest request) {
        if (auditEventRepository.existsByEventCodeAndDeletedFalse(request.eventCode())) {
            throw new DuplicateAuditEventException(request.eventCode());
        }
    }
}
