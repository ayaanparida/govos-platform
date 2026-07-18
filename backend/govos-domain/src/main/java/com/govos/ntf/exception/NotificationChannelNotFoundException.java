package com.govos.ntf.exception;

import java.util.UUID;

public class NotificationChannelNotFoundException extends NtfException {

    public NotificationChannelNotFoundException(UUID id) {
        super("Notification channel not found with id: " + id);
    }

    public NotificationChannelNotFoundException(String code) {
        super("Notification channel not found with code: " + code);
    }
}
