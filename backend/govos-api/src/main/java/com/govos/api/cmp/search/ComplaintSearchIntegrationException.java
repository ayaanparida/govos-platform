package com.govos.api.cmp.search;

import com.govos.cmp.exception.ComplaintException;

public class ComplaintSearchIntegrationException extends ComplaintException {

    public ComplaintSearchIntegrationException(String message) {
        super(message);
    }

    public ComplaintSearchIntegrationException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
