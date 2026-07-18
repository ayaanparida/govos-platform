package com.govos.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDesignationRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 200)
        String title,
        @Size(max = 50)
        String grade,
        @Size(max = 1000)
        String description,
        Boolean active
) {
}
