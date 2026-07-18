package com.govos.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateDepartmentRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotNull
        UUID organizationId,
        UUID parentDepartmentId,
        @NotBlank @Size(max = 255)
        String name,
        @Size(max = 1000)
        String description,
        Boolean active,
        Long version
) {
}
