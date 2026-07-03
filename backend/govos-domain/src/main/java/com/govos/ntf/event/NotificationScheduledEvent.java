package com.govos.ntf.event;

import java.time.Instant;
import java.util.UUID;

public record NotificationScheduledEvent(
        UUID notificationId,
        Instant scheduledAt,
        Instant occurredAt
) {
}
