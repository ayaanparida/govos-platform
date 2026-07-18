package com.govos.api.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token payload for session logout")
public record LogoutRequest(
        @NotBlank
        @Schema(description = "Refresh token to revoke", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        String refreshToken
) {
}
