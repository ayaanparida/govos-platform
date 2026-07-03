package com.govos.mdm.dto;

import java.time.Instant;
import java.util.UUID;

public record MasterDataDto(
        UUID id,
        String code,
        String type,
        String key,
        String value,
        String description,
        Integer displayOrder,
        Boolean systemDefined,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
