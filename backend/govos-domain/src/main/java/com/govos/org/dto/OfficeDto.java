package com.govos.org.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OfficeDto(
        UUID id,
        String code,
        UUID departmentId,
        String officeName,
        String address,
        String district,
        String state,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
