package com.govos.ntf.exception;

import java.util.UUID;

public class NotificationTemplateNotFoundException extends NtfException {

    public NotificationTemplateNotFoundException(UUID id) {
        super("Notification template not found with id: " + id);
    }

    public NotificationTemplateNotFoundException(String code) {
        super("Notification template not found with code: " + code);
    }
}
