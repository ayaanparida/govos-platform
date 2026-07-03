package com.govos.ntf.dto;

import com.govos.ntf.entity.NotificationPriority;
import com.govos.ntf.entity.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        String code,
        String recipient,
        String subject,
        String body,
        UUID channelId,
        NotificationStatus status,
        NotificationPriority priority,
        Instant scheduledAt,
        Instant sentAt,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
