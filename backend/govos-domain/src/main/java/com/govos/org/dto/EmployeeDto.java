package com.govos.org.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeDto(
        UUID id,
        String code,
        UUID userId,
        UUID organizationId,
        UUID departmentId,
        UUID officeId,
        UUID designationId,
        String employeeNumber,
        LocalDate joiningDate,
        LocalDate retirementDate,
        String officialEmail,
        String officialMobile,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
