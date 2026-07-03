package com.govos.ntf.dto;

import com.govos.ntf.entity.DeliveryStatus;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateNotificationDeliveryRequest(
        @Size(max = 100)
        String code,
        DeliveryStatus deliveryStatus,
        @Size(max = 255)
        String providerReference,
        Integer attemptCount,
        Instant lastAttempt,
        Boolean active,
        Long version
) {
}
