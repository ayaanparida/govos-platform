package com.govos.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateEmployeeRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID userId,
        @NotNull
        UUID organizationId,
        @NotNull
        UUID departmentId,
        UUID officeId,
        @NotNull
        UUID designationId,
        @NotBlank @Size(max = 50)
        String employeeNumber,
        LocalDate joiningDate,
        LocalDate retirementDate,
        @Size(max = 255)
        String officialEmail,
        @Size(max = 20)
        String officialMobile,
        Boolean active,
        Long version
) {
}
