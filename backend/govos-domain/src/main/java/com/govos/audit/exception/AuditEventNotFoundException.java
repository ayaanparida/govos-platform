package com.govos.audit.exception;

import java.util.UUID;

public class AuditEventNotFoundException extends AuditException {

    public AuditEventNotFoundException(UUID id) {
        super("Audit event not found with id: " + id);
    }

    public AuditEventNotFoundException(String eventCode) {
        super("Audit event not found with eventCode: " + eventCode);
    }
}
