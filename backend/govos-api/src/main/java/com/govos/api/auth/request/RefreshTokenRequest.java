package com.govos.api.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token payload for issuing a new access token")
public record RefreshTokenRequest(
        @NotBlank
        @Schema(description = "Opaque refresh token issued at login", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        String refreshToken
) {
}
