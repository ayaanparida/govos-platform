package com.govos.audit.exception;

public class DuplicateAuditEventException extends AuditException {

    public DuplicateAuditEventException(String eventCode) {
        super("Audit event code already exists: " + eventCode);
    }
}
