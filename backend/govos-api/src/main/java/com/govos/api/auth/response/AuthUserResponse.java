package com.govos.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Authenticated user summary returned with token payloads")
public record AuthUserResponse(
        @Schema(description = "User identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID userId,
        @Schema(description = "Login username", example = "jdoe")
        String username,
        @Schema(description = "Primary email address", example = "john.doe@gov.example")
        String email,
        @Schema(description = "Assigned role codes", example = "[\"OFFICER\"]")
        List<String> roles,
        @Schema(description = "Effective permission codes", example = "[\"idm:user:read\"]")
        List<String> permissions
) {
}
