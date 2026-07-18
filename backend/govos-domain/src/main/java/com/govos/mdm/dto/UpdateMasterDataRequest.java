package com.govos.mdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMasterDataRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 100)
        String type,
        @NotBlank @Size(max = 200)
        String key,
        @NotBlank @Size(max = 500)
        String value,
        @Size(max = 1000)
        String description,
        Integer displayOrder,
        Boolean systemDefined,
        Boolean active,
        Long version
) {
}
