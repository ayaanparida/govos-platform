package com.govos.ntf.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationSubscriptionDto(
        UUID id,
        String code,
        UUID userId,
        String eventType,
        UUID channelId,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
