package com.govos.ntf.dto;

import com.govos.ntf.entity.NotificationPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateNotificationQueueRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID notificationId,
        NotificationPriority priority,
        Instant nextExecution,
        Integer retryCount,
        Boolean active
) {
}
