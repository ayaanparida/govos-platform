package com.govos.audit.exception;

import java.util.UUID;

public class AuditActorNotFoundException extends AuditException {

    public AuditActorNotFoundException(UUID id) {
        super("Audit actor not found with id: " + id);
    }
}
