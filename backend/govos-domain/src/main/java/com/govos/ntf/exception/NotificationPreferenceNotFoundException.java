package com.govos.ntf.exception;

import java.util.UUID;

public class NotificationPreferenceNotFoundException extends NtfException {

    public NotificationPreferenceNotFoundException(UUID id) {
        super("Notification preference not found with id: " + id);
    }
}
