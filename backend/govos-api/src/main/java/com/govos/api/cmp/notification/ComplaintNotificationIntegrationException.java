package com.govos.api.cmp.notification;

import com.govos.cmp.exception.ComplaintException;

public class ComplaintNotificationIntegrationException extends ComplaintException {

    public ComplaintNotificationIntegrationException(String message) {
        super(message);
    }

    public ComplaintNotificationIntegrationException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
