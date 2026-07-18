package com.govos.api.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Username and password credentials for platform login")
public record LoginRequest(
        @NotBlank
        @Schema(description = "Unique login identifier", example = "jdoe")
        String username,
        @NotBlank
        @Schema(description = "User password", example = "Secret123!")
        String password
) {
}
