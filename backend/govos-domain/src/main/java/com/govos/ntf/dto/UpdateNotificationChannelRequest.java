package com.govos.ntf.dto;

import com.govos.ntf.entity.ChannelProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateNotificationChannelRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String name,
        @NotNull
        ChannelProvider provider,
        Boolean active,
        Long version
) {
}
