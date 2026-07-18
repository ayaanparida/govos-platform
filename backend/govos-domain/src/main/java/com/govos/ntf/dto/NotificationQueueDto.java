package com.govos.ntf.dto;

import com.govos.ntf.entity.NotificationPriority;

import java.time.Instant;
import java.util.UUID;

public record NotificationQueueDto(
        UUID id,
        String code,
        UUID notificationId,
        NotificationPriority priority,
        Integer retryCount,
        Integer maxRetry,
        Instant nextRetryAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
