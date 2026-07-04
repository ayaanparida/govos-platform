package com.govos.audit.exception;

import java.util.UUID;

public class AuditSessionNotFoundException extends AuditException {

    public AuditSessionNotFoundException(UUID id) {
        super("Audit session not found with id: " + id);
    }

    public AuditSessionNotFoundException(String sessionId) {
        super("Audit session not found with sessionId: " + sessionId);
    }
}
