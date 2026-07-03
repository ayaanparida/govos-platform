package com.govos.ntf.exception;

import java.util.UUID;

public class NotificationNotFoundException extends NtfException {

    public NotificationNotFoundException(UUID id) {
        super("Notification not found with id: " + id);
    }

    public NotificationNotFoundException(String code) {
        super("Notification not found with code: " + code);
    }
}
