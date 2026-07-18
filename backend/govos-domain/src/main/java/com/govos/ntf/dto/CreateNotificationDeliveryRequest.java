package com.govos.ntf.dto;

import com.govos.ntf.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateNotificationDeliveryRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID notificationId,
        DeliveryStatus deliveryStatus,
        @Size(max = 255)
        String providerReference,
        Integer retryCount,
        Integer maxRetry,
        Instant nextRetryAt,
        Instant lastAttempt,
        Boolean active
) {
}
