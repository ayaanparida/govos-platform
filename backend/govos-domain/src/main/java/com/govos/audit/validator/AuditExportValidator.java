package com.govos.audit.validator;

import com.govos.audit.dto.CreateAuditExportRequest;
import com.govos.audit.dto.UpdateAuditExportRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditExportValidator {

    public void validateCreate(CreateAuditExportRequest request) {
        // No additional business validation beyond request constraints.
    }

    public void validateUpdate(UUID id, UpdateAuditExportRequest request) {
        // No additional business validation beyond request constraints.
    }
}
