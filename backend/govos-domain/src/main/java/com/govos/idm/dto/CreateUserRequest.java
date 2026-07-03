package com.govos.idm.dto;

import com.govos.idm.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateUserRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 100)
        String username,
        @NotBlank @Email @Size(max = 255)
        String email,
        @Size(max = 20)
        String mobileNumber,
        @NotBlank @Size(max = 255)
        String passwordHash,
        @NotBlank @Size(max = 100)
        String firstName,
        @Size(max = 100)
        String middleName,
        @NotBlank @Size(max = 100)
        String lastName,
        @Size(max = 20)
        String gender,
        LocalDate dateOfBirth,
        UserStatus status,
        Boolean active
) {
}
