package com.govos.audit.validator;

import com.govos.audit.dto.CreateAuditActorRequest;
import com.govos.audit.dto.UpdateAuditActorRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditActorValidator {

    public void validateCreate(CreateAuditActorRequest request) {
        // No additional business validation beyond request constraints.
    }

    public void validateUpdate(UUID id, UpdateAuditActorRequest request) {
        // No additional business validation beyond request constraints.
    }
}
