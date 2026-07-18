package com.govos.ntf.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateNotificationPreferenceRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID userId,
        @NotNull
        UUID channelId,
        Boolean enabled,
        Boolean active
) {
}
