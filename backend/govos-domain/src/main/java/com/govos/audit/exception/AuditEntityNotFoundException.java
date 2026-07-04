package com.govos.audit.exception;

import java.util.UUID;

public class AuditEntityNotFoundException extends AuditException {

    public AuditEntityNotFoundException(UUID id) {
        super("Audit entity not found with id: " + id);
    }

    public AuditEntityNotFoundException(String entityType, UUID entityId) {
        super("Audit entity not found with type: " + entityType + ", entityId: " + entityId);
    }
}
