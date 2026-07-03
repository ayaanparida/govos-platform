package com.govos.ntf.dto;

import com.govos.ntf.entity.ChannelProvider;

import java.time.Instant;
import java.util.UUID;

public record NotificationChannelDto(
        UUID id,
        String code,
        String name,
        ChannelProvider provider,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
