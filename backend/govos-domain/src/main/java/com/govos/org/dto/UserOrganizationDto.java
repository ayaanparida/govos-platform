package com.govos.org.dto;

import java.time.Instant;
import java.util.UUID;

public record UserOrganizationDto(
        UUID id,
        String code,
        UUID userId,
        UUID organizationId,
        Boolean defaultOrganization,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
