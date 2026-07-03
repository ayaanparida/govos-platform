package com.govos.ntf.dto;

import com.govos.ntf.entity.DeliveryStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationDeliveryDto(
        UUID id,
        String code,
        UUID notificationId,
        DeliveryStatus deliveryStatus,
        String providerReference,
        Integer attemptCount,
        Instant lastAttempt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
