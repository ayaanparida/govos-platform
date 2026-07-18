package com.govos.ntf.dto;

import com.govos.ntf.entity.NotificationPriority;
import com.govos.ntf.entity.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record UpdateNotificationRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 500)
        String recipient,
        @Size(max = 500)
        String subject,
        String body,
        @NotNull
        UUID channelId,
        NotificationStatus status,
        NotificationPriority priority,
        Instant scheduledAt,
        Instant sentAt,
        Boolean active,
        Long version
) {
}
