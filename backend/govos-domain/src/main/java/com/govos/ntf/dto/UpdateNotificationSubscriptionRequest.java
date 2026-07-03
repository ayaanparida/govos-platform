package com.govos.ntf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateNotificationSubscriptionRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 100)
        String eventType,
        @NotNull
        UUID channelId,
        Boolean active,
        Long version
) {
}
