package com.govos.ntf.event;

import com.govos.ntf.entity.DeliveryStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationDeliveryRecordedEvent(
        UUID notificationId,
        UUID deliveryId,
        DeliveryStatus deliveryStatus,
        Instant occurredAt
) {
}
