package com.govos.audit.exception;

import java.util.UUID;

public class AuditExportNotFoundException extends AuditException {

    public AuditExportNotFoundException(UUID id) {
        super("Audit export not found with id: " + id);
    }
}
