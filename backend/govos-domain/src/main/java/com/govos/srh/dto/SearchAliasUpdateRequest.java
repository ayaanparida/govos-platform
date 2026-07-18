package com.govos.srh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SearchAliasUpdateRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String aliasName,
        @NotBlank @Size(max = 255)
        String physicalIndexName,
        Boolean activeAlias,
        Boolean active,
        @NotNull
        Long version
) {
}
