package com.govos.api.cmp.workflow;

import com.govos.cmp.exception.ComplaintException;

public class ComplaintWorkflowIntegrationException extends ComplaintException {

    public ComplaintWorkflowIntegrationException(String message) {
        super(message);
    }

    public ComplaintWorkflowIntegrationException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
