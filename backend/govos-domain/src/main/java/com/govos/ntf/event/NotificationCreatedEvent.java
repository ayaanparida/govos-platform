package com.govos.ntf.event;

import com.govos.ntf.entity.NotificationPriority;

import java.time.Instant;
import java.util.UUID;

public record NotificationCreatedEvent(
        UUID notificationId,
        String code,
        String recipient,
        UUID channelId,
        NotificationPriority priority,
        Instant occurredAt
) {
}
