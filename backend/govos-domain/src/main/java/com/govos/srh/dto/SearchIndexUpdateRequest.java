package com.govos.srh.dto;

import com.govos.srh.enums.SearchEngineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SearchIndexUpdateRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String name,
        @Size(max = 2000)
        String description,
        SearchEngineType engineType,
        @Positive
        Integer mappingVersion,
        @Size(max = 255)
        String aliasName,
        Boolean active,
        @NotNull
        Long version
) {
}
