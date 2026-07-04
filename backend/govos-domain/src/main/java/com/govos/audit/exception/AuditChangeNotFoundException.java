package com.govos.audit.exception;

import java.util.UUID;

public class AuditChangeNotFoundException extends AuditException {

    public AuditChangeNotFoundException(UUID id) {
        super("Audit change not found with id: " + id);
    }
}
