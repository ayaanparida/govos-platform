package com.govos.api.cmp.audit;

import com.govos.cmp.exception.ComplaintException;

public class ComplaintAuditIntegrationException extends ComplaintException {

    public ComplaintAuditIntegrationException(String message) {
        super(message);
    }

    public ComplaintAuditIntegrationException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
