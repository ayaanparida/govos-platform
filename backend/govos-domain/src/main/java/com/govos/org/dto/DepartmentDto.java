package com.govos.org.dto;

import java.time.Instant;
import java.util.UUID;

public record DepartmentDto(
        UUID id,
        String code,
        UUID organizationId,
        UUID parentDepartmentId,
        String name,
        String description,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
