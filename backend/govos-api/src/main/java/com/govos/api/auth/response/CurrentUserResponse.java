package com.govos.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Authenticated user profile and effective authorities")
public record CurrentUserResponse(
        @Schema(description = "Whether the caller is authenticated", example = "false")
        boolean authenticated,
        @Schema(description = "User identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID userId,
        @Schema(description = "Login username", example = "jdoe")
        String username,
        @Schema(description = "Primary email address", example = "john.doe@gov.example")
        String email,
        @Schema(description = "Assigned role codes", example = "[\"OFFICER\", \"ADMIN\"]")
        List<String> roles,
        @Schema(description = "Effective permission codes", example = "[\"idm:user:read\", \"cmp:complaint:create\"]")
        List<String> permissions
) {
}
