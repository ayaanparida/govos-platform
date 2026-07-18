package com.govos.ntf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import java.util.UUID;

public record UpdateNotificationTemplateRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String name,
        @NotNull
        UUID channelId,
        @Size(max = 500)
        String subjectTemplate,
        String bodyTemplate,
        List<String> templateVariables,
        Boolean active,
        Long version
) {
}
