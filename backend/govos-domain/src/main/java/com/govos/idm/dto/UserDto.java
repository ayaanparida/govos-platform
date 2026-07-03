package com.govos.idm.dto;

import com.govos.idm.entity.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserDto(
        UUID id,
        String code,
        String username,
        String email,
        String mobileNumber,
        String firstName,
        String middleName,
        String lastName,
        String gender,
        LocalDate dateOfBirth,
        UserStatus status,
        Boolean accountLocked,
        Integer failedLoginAttempts,
        Instant lastLogin,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
