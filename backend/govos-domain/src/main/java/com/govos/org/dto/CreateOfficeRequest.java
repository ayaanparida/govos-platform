package com.govos.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOfficeRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotNull
        UUID departmentId,
        @NotBlank @Size(max = 255)
        String officeName,
        @Size(max = 1000)
        String address,
        @Size(max = 100)
        String district,
        @Size(max = 100)
        String state,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean active
) {
}
