package com.govos.ntf.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationTemplateDto(
        UUID id,
        String code,
        String name,
        UUID channelId,
        String subjectTemplate,
        String bodyTemplate,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
