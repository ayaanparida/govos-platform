package com.govos.idm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 150)
        String name,
        @Size(max = 1000)
        String description,
        Boolean systemRole,
        Boolean active
) {
}
