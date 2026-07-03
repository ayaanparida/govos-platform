package com.govos.ntf.exception;

import java.util.UUID;

public class NotificationDeliveryNotFoundException extends NtfException {

    public NotificationDeliveryNotFoundException(UUID id) {
        super("Notification delivery not found with id: " + id);
    }
}
