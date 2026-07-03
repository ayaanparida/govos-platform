package com.govos.ntf.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateNotificationPreferenceRequest(
        @Size(max = 100)
        String code,
        @NotNull
        Boolean enabled,
        Boolean active,
        Long version
) {
}
