package com.govos.org.dto;

import com.govos.org.entity.OrganizationStatus;

import java.time.Instant;
import java.util.UUID;

public record OrganizationDto(
        UUID id,
        String code,
        String name,
        String shortName,
        String type,
        String registrationNumber,
        String email,
        String phone,
        String website,
        OrganizationStatus status,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
