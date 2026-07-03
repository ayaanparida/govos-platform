package com.govos.idm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 100)
        String module,
        @NotBlank @Size(max = 100)
        String resource,
        @NotBlank @Size(max = 50)
        String action,
        @Size(max = 1000)
        String description,
        Boolean active
) {
}
