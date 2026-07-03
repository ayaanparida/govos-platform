package com.govos.org.dto;

import java.time.Instant;
import java.util.UUID;

public record DepartmentHierarchyDto(
        UUID id,
        String code,
        UUID parentDepartmentId,
        UUID childDepartmentId,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
