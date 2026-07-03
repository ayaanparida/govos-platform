package com.govos.ntf.exception;

import java.util.UUID;

public class NotificationQueueNotFoundException extends NtfException {

    public NotificationQueueNotFoundException(UUID id) {
        super("Notification queue entry not found with id: " + id);
    }
}
