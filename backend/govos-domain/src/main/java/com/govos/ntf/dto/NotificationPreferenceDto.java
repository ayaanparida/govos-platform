package com.govos.ntf.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationPreferenceDto(
        UUID id,
        String code,
        UUID userId,
        UUID channelId,
        Boolean enabled,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
