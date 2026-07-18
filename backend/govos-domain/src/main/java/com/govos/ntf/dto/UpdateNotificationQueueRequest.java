package com.govos.ntf.dto;

import com.govos.ntf.entity.NotificationPriority;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateNotificationQueueRequest(
        @Size(max = 100)
        String code,
        NotificationPriority priority,
        Integer retryCount,
        Integer maxRetry,
        Instant nextRetryAt,
        Boolean active,
        Long version
) {
}
