package com.govos.security.model;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank
        String username,
        @NotBlank
        String password,
        String ipAddress,
        String device,
        String browser,
        String userAgent
) {
}
