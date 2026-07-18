package com.govos.srh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SearchAliasCreateRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID searchIndexId,
        @NotBlank @Size(max = 255)
        String aliasName,
        @NotBlank @Size(max = 255)
        String physicalIndexName,
        Boolean activeAlias,
        Boolean active
) {
}
