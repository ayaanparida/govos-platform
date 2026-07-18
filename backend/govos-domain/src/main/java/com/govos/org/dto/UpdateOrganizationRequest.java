package com.govos.org.dto;

import com.govos.org.entity.OrganizationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String name,
        @Size(max = 50)
        String shortName,
        @Size(max = 50)
        String type,
        @Size(max = 100)
        String registrationNumber,
        @Size(max = 255)
        String email,
        @Size(max = 20)
        String phone,
        @Size(max = 500)
        String website,
        OrganizationStatus status,
        Boolean active,
        Long version
) {
}
