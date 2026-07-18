package com.govos.ntf.exception;

import java.util.UUID;

public class NotificationSubscriptionNotFoundException extends NtfException {

    public NotificationSubscriptionNotFoundException(UUID id) {
        super("Notification subscription not found with id: " + id);
    }
}
